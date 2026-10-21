package com.huyi.recap.feature.ai

import com.huyi.recap.BuildConfig
import com.huyi.recap.feature.voice.Whisper
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val HINT = Regex("[0-9]|mai|mốt|tuần|tháng|tết|nay|sau|trước|lúc|hạn|chủ nhật|thứ|cuối|đầu")

/** `{"at":"2026-10-24T09:00"}` → epoch ms nếu còn ở tương lai; null/sai định dạng → null. */
fun parseAiWhen(content: String, now: LocalDateTime = LocalDateTime.now()): Long? {
    val s = Regex("\"at\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\"").find(content)?.groupValues?.get(1) ?: return null
    val t = runCatching { LocalDateTime.parse(s) }.getOrNull()?.takeIf { it.isAfter(now) } ?: return null
    return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

/** remind=false: AI xác định câu này KHÔNG cần nhắc (ghi chép, việc đã qua, nhắc giờ lướt qua); at: mốc nhắc nếu có. */
class When(val remind: Boolean, val at: Long?)

/** `{"remind":false,...}` → false; thiếu/sai → null (không rõ). */
fun parseAiRemind(content: String): Boolean? =
    Regex("\"remind\"\\s*:\\s*(true|false)").find(content)?.groupValues?.get(1)?.toBooleanStrictOrNull()

/** Hỏi DeepSeek: có cần nhắc không + mốc nhắc. Không khóa / không có từ gợi ý thời gian / lỗi mạng → null (không rõ, giữ luật `parseWhen`). */
suspend fun aiWhen(text: String): When? {
    if (!HINT.containsMatchIn(text.lowercase())) return null
    val now = LocalDateTime.now()
    val sys = "Hôm nay là ${now.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy HH:mm", java.util.Locale("vi")))}. " +
        "Từ ghi chú tiếng Việt, quyết định có cần đặt nhắc lịch không. Trả JSON {\"remind\":true|false,\"at\":\"yyyy-MM-ddTHH:mm\"|null}. " +
        "remind=true CHỈ khi người nói muốn được nhắc làm/đi/nhớ việc gì đó vào một thời điểm SẮP TỚI (hẹn, deadline, việc cần làm, dặn nhớ). " +
        "remind=false khi là ghi chép thông tin, ý tưởng, cảm nghĩ, sự việc đã xảy ra, câu hỏi, hoặc chỉ nhắc đến giờ/ngày mà không cần nhắc. remind=false thì at=null. " +
        "Cụm mơ hồ thì chọn mốc hợp lý: cuối tuần = thứ bảy, đầu tuần = thứ hai, cuối tháng = ngày cuối tháng, chỉ có ngày thì 08:00, sáng 08:00, trưa 12:00, chiều 15:00, tối 19:00."
    return chat(sys, text, 60, true)?.let { c -> parseAiRemind(c)?.let { r -> When(r, if (r) parseAiWhen(c, now) else null) } }
}

/** Chữ Whisper (có thể lẫn tạp âm, lặp vòng, câu ma, sai dấu) → AI lọc và sửa lại cho đúng. Chỉ tạp âm → "". Lỗi/không khóa → null (giữ chữ gốc). */
suspend fun aiClean(text: String): String? {
    val sys = "Bạn nhận văn bản tiếng Việt do máy nhận dạng giọng nói (Whisper) chép ra, có thể lẫn tạp âm. Hãy: " +
        "1) bỏ phần là tạp âm/câu ma (vd 'Hãy subscribe cho kênh', 'Cảm ơn các bạn đã xem', '[Âm nhạc]'), từ lặp vòng, tiếng ậm ờ; " +
        "2) sửa lỗi nghe nhầm, dấu thanh, chính tả, viết hoa, dấu câu cho đúng nghĩa theo ngữ cảnh. " +
        "Đây là ghi chú việc cá nhân (họp, gọi điện, mua sắm, nhắc giờ, tiền điện nước); ưu tiên từ hợp ngữ cảnh đó (vd 'học với anh Nam' → 'họp với anh Nam' nếu có giờ họp; 'cho bé' không phải 'cho bữa'). " +
        "KHÔNG thêm ý, KHÔNG tóm tắt, KHÔNG đổi nghĩa, KHÔNG gộp/tách câu hay đổi cụm từ đúng, giữ nguyên số/giờ/tên riêng. Chỉ trả văn bản đã sửa, không lời dẫn; nếu toàn tạp âm trả chuỗi rỗng."
    if (!cleanable(text)) return null   // dài quá ⇒ giữ chữ gốc, không để chat() cắt đuôi rồi thay vào
    return chat(sys, text, 1200, false, CLEAN_MAX)?.trim()
}

internal const val CLEAN_MAX = 1500
internal fun cleanable(text: String) = text.length <= CLEAN_MAX

/** Sau khi nghe giọng nói: AI nói lại ngắn gọn điều nó hiểu (việc gì, khi nào) để người dùng soát. Lỗi/không khóa → null. */
suspend fun aiRecap(text: String): String? {
    val now = LocalDateTime.now()
    val sys = "Hôm nay là ${now.format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy HH:mm", java.util.Locale("vi")))}. " +
        "Người dùng vừa đọc một ghi chú bằng giọng nói (có thể sai chính tả do nhận dạng). Nói lại điều bạn hiểu trong 1 câu tiếng Việt ngắn (≤ 25 từ): việc gì, ở đâu/với ai nếu có. KHÔNG nêu ngày giờ (app tự hiển thị mốc nhắc riêng). Không thêm lời dẫn."
    return chat(sys, text, 80, false)?.trim()?.takeIf { it.isNotEmpty() }
}

class Brief(val title: String, val summary: String)

/** Bước ③ cho ảnh chụp: từ chữ OCR (lộn xộn) → tiêu đề ≤ 8 từ + tóm tắt 1–2 câu. Lỗi/không khóa → null. */
suspend fun aiBrief(text: String, voice: Boolean = false): Brief? {
    val sys = (if (voice) "Văn bản dưới đây là ghi chú giọng nói của người dùng (đã chép thành chữ, có thể còn sai chính tả). " else "Văn bản dưới đây là chữ OCR từ một ảnh chụp màn hình (có thể lộn xộn). ") + "Trả JSON {\"title\":\"...\",\"summary\":\"...\"}. " +
        "title: tiêu đề tiếng Việt ≤ 8 từ nói nội dung về gì, KHÔNG bắt đầu bằng 'Ghi chú'. " +
        "summary: 1–2 câu tiếng Việt (≤ 40 từ) tóm ý chính, viết như chính người ghi tự nhắc mình (câu mệnh lệnh/ngắn, vd 'Họp 9h sáng mai'); " +
        "KHÔNG dùng ngôi thứ ba, KHÔNG mở bằng 'Người dùng'. Không thêm lời dẫn."
    val c = chat(sys, text, 120, true) ?: return null
    val o = runCatching { JSONObject(c) }.getOrNull() ?: return null
    val t = o.optString("title").trim()
    return if (t.isEmpty()) null else Brief(t, o.optString("summary").trim())
}

/** Loại note (AI chỉ được chọn trong tập này). */
val KINDS = linkedMapOf("viec" to "Việc", "lich" to "Lịch hẹn", "mua" to "Mua sắm", "y-tuong" to "Ý tưởng", "dia-diem" to "Địa điểm", "hoc" to "Học tập", "khac" to "Khác")
class Analysis(val kind: String, val tags: List<String>)

private val JUNK_TAGS = setOf("mai", "nay", "hom", "qua", "kia", "sang", "trua", "chieu", "toi", "dem", "thu", "ngay", "tuan", "thang", "nam", "gio", "phut",
    "hop", "la", "va", "voi", "cua", "de", "se", "da", "can", "phai", "nho", "ghi", "chu", "nguoi", "dung")
private val JUNK_TIME = Regex("\\d+(-?(h|g|gio|ph|phut|am|pm))?(-\\d+)*|thu-\\w+|ngay-\\w+")

/** Tag rác: token thời gian (mai, 9h, 19-00, thu-hai…) hoặc stopword. */
fun isJunkTag(slug: String) = slug in JUNK_TAGS || JUNK_TIME.matches(slug)

/** `{"kind":"mua","tags":["sua","cho"]}` → loại hợp lệ (sai → "khac") + ≤3 tag slug; không phải JSON → null. */
fun parseAnalysis(content: String): Analysis? {
    if (!content.contains('{')) return null
    val kind = Regex("\"kind\"\\s*:\\s*\"([^\"]*)\"").find(content)?.groupValues?.get(1)?.takeIf { it in KINDS } ?: "khac"
    val raw = Regex("\"tags\"\\s*:\\s*\\[([^\\]]*)\\]").find(content)?.groupValues?.get(1).orEmpty()
    val tags = Regex("\"([^\"]*)\"").findAll(raw).map { it.groupValues[1].lowercase().trim().replace(Regex("[^a-z0-9]+"), "-").trim('-') }
        .filter { it.isNotEmpty() && !isJunkTag(it) }.distinct().take(3).toList()
    return Analysis(kind, tags)
}

/** Bước ③: loại + tag. Lỗi/không khóa → null. */
suspend fun aiAnalyze(text: String): Analysis? {
    val sys = "Phân tích ghi chú tiếng Việt. Trả JSON {\"kind\":\"...\",\"tags\":[...]}. kind chỉ được là một trong: " +
        "viec (việc cần làm), lich (lịch hẹn/họp), mua (mua sắm), y-tuong (ý tưởng), dia-diem (địa điểm/quán), hoc (học tập), khac. " +
        "tags: 1–3 từ khoá ngắn, tiếng Việt KHÔNG dấu, chữ thường, nối bằng dấu gạch ngang (vd \"quan-cafe\")."
    return chat(sys, text, 80, true)?.let(::parseAnalysis)
}
