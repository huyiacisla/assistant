package com.huyi.recap.feature.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.store.Db
import com.huyi.recap.store.Item
import com.huyi.recap.store.clipFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var lastClip = ""

/** Đọc clipboard hiện tại; null nếu rỗng hoặc bị chặn (app không có focus). */
fun readClip(ctx: Context): String? =
    (ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
        ?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(ctx)?.toString()?.trim()?.ifBlank { null }

private val URL = Regex("^(https?://|www\\.)\\S+$", RegexOption.IGNORE_CASE)

/** Toàn bộ clipboard là 1 URL ⇒ vào tab Link. */
fun isLink(text: String) = URL.matches(text.trim())

/** Lưu text thành Item (link nếu là 1 URL), bỏ qua nếu trùng lần trước; có Hoàn tác. */
fun saveClip(ctx: Context, text: String) {
    if (text == lastClip) return
    lastClip = text
    val app = ctx.applicationContext
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Db.get(app).dao()
        val item = Item(folderId = dao.clipFolder(), type = if (isLink(text)) ItemType.Link.db else ItemType.Text.db, text = text, source = "clipboard")
        val saved = item.copy(id = dao.insert(item))
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            OverlayService.showUndo("Đã lưu ✓", if (isLink(text)) "Đã lưu vào tab Link" else "Đã lưu clipboard · ${text.length} ký tự") {
                lastClip = ""
                CoroutineScope(Dispatchers.IO).launch { dao.delete(saved) }
            }
        }
    }
}
