package com.huyi.recap.feature.quicknote

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.huyi.recap.feature.ai.aiAnalyze
import com.huyi.recap.feature.ai.aiBrief
import com.huyi.recap.feature.ai.aiClean
import com.huyi.recap.feature.ai.aiRecap
import com.huyi.recap.feature.ai.aiWhen
import com.huyi.recap.feature.clipboard.isLink
import com.huyi.recap.feature.remind.cancelRemind
import com.huyi.recap.feature.remind.remindLabel
import com.huyi.recap.feature.remind.scheduleRemind
import com.huyi.recap.feature.voice.Whisper
import com.huyi.recap.feature.voice.Zipformer
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.store.Db
import com.huyi.recap.store.Item
import com.huyi.recap.ui.wrap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Mặc định Zipformer-vi 30M (RTF ~0,03); thiếu model hoặc careful=true ("nghe kỹ", câu lẫn tiếng Anh) → Whisper. */
internal fun QuickNoteActivity.dictate(bytes: ByteArray, careful: Boolean = false): String {
    val sb = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN).asShortBuffer()
    val f = FloatArray(sb.remaining()) { sb.get() / 32768f }
    if (!careful && Zipformer.ready(zipDir)) return Zipformer.transcribe(zipDir, f)
    require(model.exists()) { "thiếu ${model.name}" }
    return Whisper.transcribe(model.path, f)
}

internal fun QuickNoteActivity.wavHeader(n: Int) = java.nio.ByteBuffer.allocate(44).order(java.nio.ByteOrder.LITTLE_ENDIAN).apply {
    put("RIFF".toByteArray()); putInt(36 + n); put("WAVEfmt ".toByteArray()); putInt(16); putShort(1); putShort(1)
    putInt(16000); putInt(32000); putShort(2); putShort(16); put("data".toByteArray()); putInt(n)
}.array()

/** Chép chữ (offline) + AI lọc tạp âm/sửa chữ; lỗi mạng → giữ chữ gốc. */
internal fun QuickNoteActivity.transcribe(seg: ByteArray, careful: Boolean) = runCatching {
    dictate(seg, careful).trim().let { raw -> (if (raw.isEmpty()) raw else kotlinx.coroutines.runBlocking { aiClean(raw) } ?: raw).also { android.util.Log.i("Voice", "chép${if (careful) " kỹ" else ""}: \"$raw\" → \"$it\"") } }
}

/** 4 bước AI độc lập chạy song song (① hiểu ② giờ nhắc ③ loại + tag ④ tiêu đề). Bỏ kết quả nếu text đã đổi trong lúc chờ. */
internal suspend fun QuickNoteActivity.understand(t: String) = kotlinx.coroutines.coroutineScope {
    launch { val r = aiRecap(t); if (t == text) { recap = r; done = done or 1 } }
    launch { val w = aiWhen(t); if (t == text) { aiVeto = w?.remind == false; aiAt = w?.at; done = done or 2 } }
    launch { val k = aiAnalyze(t); if (t == text) { analysis = k; done = done or 4 } }
    launch { val b = aiBrief(t, true); if (t == text) { brief = b; done = done or 8 } }
}

internal fun QuickNoteActivity.save() {
    val app = applicationContext; val t = text.trim()
    if (t.isEmpty() && img == null && audio == null) return
    kept = true
    val path = img; val voice = audio; val known = analysis; val knownBrief = brief; val when_ = chosen
    CoroutineScope(Dispatchers.IO).launch {
        val dao = Db.get(app).dao()
        val at = remindAt(t)
        val an = known ?: if (t.isEmpty()) null else aiAnalyze(t)   // bước ③ chưa kịp chạy → chạy nốt (lỗi → để trống)
        val bf = knownBrief ?: if (t.isEmpty()) null else aiBrief(t, true)   // ④ chưa kịp → chạy nốt
        val saved = Item(type = if (isLink(t)) ItemType.Link.db else ItemType.Note.db, text = t, title = bf?.title ?: "", summary = bf?.summary ?: "", imagePath = path, audioPath = voice, source = "voice", remindAt = at, createdAt = when_ ?: System.currentTimeMillis(),
            kind = an?.kind ?: "", tags = an?.tags?.joinToString(" ") ?: "").let { it.copy(id = dao.insert(it)) }
        if (at != null) scheduleRemind(app, saved.id, at, t)
        else if (t.isNotEmpty() && !dismissed && !aiVeto) aiWhen(t)?.at?.let { ai ->   // luật không bắt được → hỏi DeepSeek (offline/lỗi → bỏ qua)
            dao.update(saved.copy(remindAt = ai)); scheduleRemind(app, saved.id, ai, t)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                OverlayService.showUndo("Đã đặt nhắc ✓", "AI: nhắc lúc ${remindLabel(ai)}") { CoroutineScope(Dispatchers.IO).launch { dao.update(saved); cancelRemind(app, saved.id) } }
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            OverlayService.showUndo("Đã lưu ✓", if (at != null) "Nhắc lúc ${remindLabel(at)}" else "Ghi chú · ${t.length} ký tự") {
                CoroutineScope(Dispatchers.IO).launch { dao.delete(saved); cancelRemind(app, saved.id) }
            }
        }
    }
    finish()
}
