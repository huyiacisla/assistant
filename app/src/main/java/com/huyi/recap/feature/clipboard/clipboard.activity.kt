package com.huyi.recap.feature.clipboard

import android.app.Activity
import android.content.ClipboardManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import com.huyi.recap.feature.capture.saveShot

/** Dự phòng khi đọc nền bị chặn: activity trong suốt lấy focus, đọc clipboard rồi tự đóng. */
class ClipboardActivity : Activity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b) }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus) return
        val clip = (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).primaryClip
        val uri = clip?.takeIf { it.itemCount > 0 && it.description.hasMimeType("image/*") }?.getItemAt(0)?.uri
        val bmp = uri?.let { runCatching { contentResolver.openInputStream(it)?.use(BitmapFactory::decodeStream) }.getOrNull() }
        Log.i("Recap", "ClipboardActivity: ảnh=${bmp != null} mime=${(0 until (clip?.description?.mimeTypeCount ?: 0)).map { clip!!.description.getMimeType(it) }} uri=$uri items=${clip?.itemCount}")
        if (bmp != null) saveShot(this, bmp, "Clip", System.currentTimeMillis(), "clipboard")
        else readClip(this)?.let { saveClip(this, it) }
        finish()
    }
}
