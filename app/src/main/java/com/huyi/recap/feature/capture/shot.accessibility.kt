package com.huyi.recap.feature.capture

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.huyi.recap.overlay.OverlayService

/** Spike B: chụp 1 chạm, không hộp thoại đồng ý. Chỉ dùng takeScreenshot, không đọc cây view. */
class ShotAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { instance = this }
    override fun onUnbind(intent: android.content.Intent?): Boolean { instance = null; return super.onUnbind(intent) }
    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun shoot() {
        val t0 = System.currentTimeMillis()
        takeScreenshot(Display.DEFAULT_DISPLAY, mainExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(r: ScreenshotResult) {
                val hw = Bitmap.wrapHardwareBuffer(r.hardwareBuffer, r.colorSpace)
                val bmp = hw?.copy(Bitmap.Config.ARGB_8888, false)
                r.hardwareBuffer.close()
                if (bmp == null) return fail("bitmap null")
                saveShot(this@ShotAccessibilityService, bmp, "A11y", t0)
            }
            override fun onFailure(code: Int) = fail("code $code")
        })
    }

    private fun fail(why: String) {
        OverlayService.setBubbleVisible(true)
        Toast.makeText(this, "Chụp lỗi: $why", Toast.LENGTH_SHORT).show()
    }

    companion object {
        var instance: ShotAccessibilityService? = null }
}
