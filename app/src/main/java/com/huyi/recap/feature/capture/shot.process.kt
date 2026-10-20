package com.huyi.recap.feature.capture

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.huyi.recap.feature.ai.KINDS
import com.huyi.recap.feature.ai.aiAnalyze
import com.huyi.recap.feature.ai.aiBrief
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.store.Db
import com.huyi.recap.store.Item
import com.huyi.recap.store.clipFolder
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Dùng chung cho cả 2 đường chụp: lưu JPEG → OCR → Item(type=image) → toast. */
fun saveShot(ctx: Context, bmp: Bitmap, via: String, t0: Long, source: String = "screenshot", done: () -> Unit = {}) {
    val app = ctx.applicationContext
    val main = Handler(Looper.getMainLooper())
    val step = OverlayService.showSteps(listOf("Chụp màn hình", "Đọc chữ trong ảnh", "AI đặt tiêu đề & tóm tắt", "Phân loại & gắn thẻ", "Lưu vào Kho"))
    step(0)
    val f = File(app.filesDir, "img_${System.currentTimeMillis()}.jpg")
    f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    val t1 = System.currentTimeMillis()
    step(1)
    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        .process(InputImage.fromBitmap(bmp, 0))
        .addOnCompleteListener { task ->
            val text = if (task.isSuccessful) task.result.text.trim() else ""
            val now = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                val dao = Db.get(app).dao()
                main.post { step(2) }
                val brief = if (text.isBlank()) null else aiBrief(text)
                main.post { step(3) }
                val an = if (text.isBlank()) null else aiAnalyze(text)
                main.post { step(4) }
                val item = Item(folderId = if (source == "clipboard") dao.clipFolder() else null, type = ItemType.Image.db, text = text, imagePath = f.absolutePath, source = source, title = brief?.title ?: "", summary = brief?.summary ?: "",
                    kind = an?.kind ?: "", tags = an?.tags?.joinToString(" ") ?: "", aiStatus = if (an != null) "done" else if (text.isBlank()) "done" else "error")
                val saved = item.copy(id = dao.insert(item))
                main.post {
                    step(-1)
                    OverlayService.setBubbleVisible(true)
                    Log.i("Recap", "[$via] lưu ${now - t0}ms (OCR ${now - t1}ms, ${text.length} ký tự)")
                    OverlayService.showUndo("Đã lưu ✓", if (text.isBlank()) "[$via] ${now - t0}ms · ảnh không có chữ" else (brief?.title ?: "AI đã đọc chữ trong ảnh") + " · ${an?.let { KINDS[it.kind] } ?: "chưa phân loại"} · ${text.length} ký tự", android.graphics.Bitmap.createScaledBitmap(bmp, 72, (72f * bmp.height / bmp.width).toInt().coerceAtLeast(1), true)) {
                        CoroutineScope(Dispatchers.IO).launch { dao.delete(saved); f.delete() }
                    }
                    done()
                }
            }
        }
}
