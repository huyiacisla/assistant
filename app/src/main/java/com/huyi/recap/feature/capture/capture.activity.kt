package com.huyi.recap.feature.capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionConfig
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.huyi.recap.overlay.OverlayService

/** Trong suốt: xin quyền MediaProjection (mỗi lần chụp, Android 14+) rồi giao cho CaptureService. */
class CaptureActivity : ComponentActivity() {
    private val consent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK && r.data != null) {
            OverlayService.setBubbleVisible(false)
            startForegroundService(
                Intent(this, CaptureService::class.java).putExtra("code", r.resultCode).putExtra("data", r.data)
            )
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            // toàn màn hình (Android 14+ mặc định cho chọn 1 app → chụp ra ảnh của app chọn, không phải màn hình hiện tại)
            val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            consent.launch(mpm.createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay()))
        }
    }
}
