package com.huyi.recap.sync

import com.huyi.recap.store.Item
import com.huyi.recap.store.KhoDao
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Sao lưu 1 chiều lên Drive: thư mục "Recap" chứa recap.json (ghi đè) + ảnh (bỏ qua ảnh đã có theo tên). */
object DriveSync {
    private const val API = "https://www.googleapis.com"

    fun toJson(all: List<Item>): String = JSONArray().also { a ->
        all.sortedBy { it.id }.forEach { i ->
            a.put(JSONObject().put("uid", i.uid).put("updatedAt", i.updatedAt).put("type", i.type).put("text", i.text).put("image", i.imagePath?.let { File(it).name })
                .put("source", i.source).put("createdAt", i.createdAt).put("folderId", i.folderId).put("tags", i.tags).put("title", i.title).put("summary", i.summary)
                .put("aiStatus", i.aiStatus).put("remindAt", i.remindAt).put("status", i.status).put("kind", i.kind).put("deletedAt", i.deletedAt))
        }
    }.toString(1)

    /** Trả (số mục, số ảnh mới). */
    suspend fun backup(token: String, all: List<Item>): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val folder = find(token, "name='Recap' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            ?: create(token, JSONObject().put("name", "Recap").put("mimeType", "application/vnd.google-apps.folder"), null, null)
        val have = list(token, "'$folder' in parents and trashed=false")
        val json = toJson(all).toByteArray()
        have["recap.json"]?.let { update(token, it, json, "application/json") }
            ?: create(token, JSONObject().put("name", "recap.json").put("parents", JSONArray().put(folder)), json, "application/json")
        var up = 0
        all.mapNotNull { i -> i.imagePath?.let(::File)?.takeIf { it.isFile && it.name !in have } }.distinctBy { it.name }.forEach { f ->
            create(token, JSONObject().put("name", f.name).put("parents", JSONArray().put(folder)), f.readBytes(), "image/jpeg"); up++
        }
        all.size to up
    }

    /** Gộp từ Drive: thêm mục chưa có (khoá createdAt+text), tải ảnh thiếu vào filesDir. Thư mục (folderId) không sao lưu ⇒ về "chưa phân loại". Trả (mục thêm, ảnh tải). */
    suspend fun restore(ctx: android.content.Context, token: String, dao: com.huyi.recap.store.KhoDao): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val folder = find(token, "name='Recap' and mimeType='application/vnd.google-apps.folder' and trashed=false") ?: error("Chưa có bản sao lưu trên Drive")
        val have = list(token, "'$folder' in parents and trashed=false")
        val arr = JSONArray(download(token, have["recap.json"] ?: error("Thiếu recap.json")).decodeToString())
        val keys = dao.keys().toHashSet()
        val uids = dao.uids().toHashSet()
        var add = 0; var img = 0
        for (k in 0 until arr.length()) {
            val o = arr.getJSONObject(k)
            val key = "${o.getLong("createdAt")}:${o.getString("text")}"
            val uid = o.optString("uid")  // bản sao lưu cũ chưa có uid ⇒ dựa khoá createdAt+text
            if (uid.isNotEmpty() && !uids.add(uid) || uid.isEmpty() && !keys.add(key)) continue
            val path = o.optString("image").takeIf { it.isNotEmpty() && o.opt("image") != JSONObject.NULL }?.let { n ->
                val f = File(ctx.filesDir, n)
                if (!f.exists()) have[n]?.let { f.writeBytes(download(token, it)); img++ }
                f.takeIf { it.exists() }?.absolutePath
            }
            fun s(n: String) = if (o.isNull(n)) null else o.getString(n)
            fun l(n: String) = if (o.isNull(n)) null else o.getLong(n)
            dao.insert(Item(type = o.getString("type"), text = o.getString("text"), imagePath = path, source = s("source"), createdAt = o.getLong("createdAt"),
                uid = uid.ifEmpty { java.util.UUID.randomUUID().toString() }, updatedAt = o.optLong("updatedAt", o.getLong("createdAt")), tags = o.optString("tags"), title = o.optString("title"), summary = o.optString("summary"), aiStatus = o.optString("aiStatus", "pending"), remindAt = l("remindAt"), status = o.optString("status", "open"),
                kind = o.optString("kind"), deletedAt = l("deletedAt")))
            add++
        }
        add to img
    }

    private fun download(t: String, id: String): ByteArray {
        val c = URL("$API/drive/v3/files/$id?alt=media").openConnection() as HttpURLConnection
        c.setRequestProperty("Authorization", "Bearer $t")
        try { if (c.responseCode != 200) error("Drive ${c.responseCode}"); return c.inputStream.readBytes() } finally { c.disconnect() }
    }

    private fun q(s: String) = URLEncoder.encode(s, "UTF-8")

    private fun call(token: String, method: String, url: String, ctype: String?, body: ByteArray?): JSONObject {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = if (method == "PATCH") "POST" else method
        if (method == "PATCH") c.setRequestProperty("X-HTTP-Method-Override", "PATCH")
        c.setRequestProperty("Authorization", "Bearer $token")
        try {
            if (body != null) { c.doOutput = true; c.setRequestProperty("Content-Type", ctype); c.outputStream.use { it.write(body) } }
            if (c.responseCode !in 200..299) error("Drive ${c.responseCode} ${c.errorStream?.bufferedReader()?.readText()?.take(120)}")
            return JSONObject(c.inputStream.bufferedReader().readText())
        } finally { c.disconnect() }
    }

    private fun find(t: String, query: String) =
        call(t, "GET", "$API/drive/v3/files?fields=files(id)&q=${q(query)}", null, null).getJSONArray("files").optJSONObject(0)?.getString("id")

    private fun list(t: String, query: String): Map<String, String> =
        call(t, "GET", "$API/drive/v3/files?pageSize=1000&fields=files(id,name)&q=${q(query)}", null, null).getJSONArray("files")
            .let { a -> (0 until a.length()).associate { a.getJSONObject(it).getString("name") to a.getJSONObject(it).getString("id") } }

    private fun update(t: String, id: String, data: ByteArray, mime: String) {
        call(t, "PATCH", "$API/upload/drive/v3/files/$id?uploadType=media", mime, data)
    }

    private fun create(t: String, meta: JSONObject, data: ByteArray?, mime: String?): String {
        if (data == null) return call(t, "POST", "$API/drive/v3/files?fields=id", "application/json", meta.toString().toByteArray()).getString("id")
        val b = "recapbnd"
        val head = "--$b\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n$meta\r\n--$b\r\nContent-Type: $mime\r\n\r\n".toByteArray()
        return call(t, "POST", "$API/upload/drive/v3/files?uploadType=multipart&fields=id", "multipart/related; boundary=$b", head + data + "\r\n--$b--".toByteArray()).getString("id")
    }
}
