package com.huyi.recap.feature.capture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.huyi.recap.R
import com.huyi.recap.feature.quicknote.save
import com.huyi.recap.overlay.OverlayService

/** Chụp 1 khung hình → lưu JPEG → OCR → Item(type=image). Xong thì tự dừng. */
class CaptureService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private var t0 = 0L

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("capture", "Chụp màn hình", NotificationManager.IMPORTANCE_MIN))
        startForeground(
            2,
            Notification.Builder(this, "capture").setSmallIcon(android.R.drawable.ic_menu_camera).setContentTitle("Đang chụp").build(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
        )
        t0 = System.currentTimeMillis()
        val code = intent?.getIntExtra("code", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data")
        if (data == null) return finish()

        val mp = (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).getMediaProjection(code, data) ?: return finish()
        mp.registerCallback(object : MediaProjection.Callback() {}, main) // bắt buộc từ Android 14
        // chờ hộp thoại đồng ý tắt hẳn + bubble ẩn khỏi khung hình
        main.postDelayed({ grab(mp) }, 600)
        return START_NOT_STICKY
    }

    private fun grab(mp: MediaProjection) {
        val m = resources.displayMetrics
        val reader = ImageReader.newInstance(m.widthPixels, m.heightPixels, PixelFormat.RGBA_8888, 2)
        val vd = mp.createVirtualDisplay("recap", m.widthPixels, m.heightPixels, m.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, reader.surface, null, main) ?: return mp.stop()
        reader.setOnImageAvailableListener({ r ->
            val img = r.acquireLatestImage() ?: return@setOnImageAvailableListener
            r.setOnImageAvailableListener(null, null)
            val p = img.planes[0]
            val w = p.rowStride / p.pixelStride
            val bmp = Bitmap.createBitmap(w, m.heightPixels, Bitmap.Config.ARGB_8888).also { it.copyPixelsFromBuffer(p.buffer) }
            img.close(); vd.release(); reader.close(); mp.stop()
            save(Bitmap.createBitmap(bmp, 0, 0, m.widthPixels, m.heightPixels))
        }, main)
    }

    private fun save(bmp: Bitmap) = saveShot(this, bmp, "MP", t0, done = { finish() })

    private fun finish(): Int {
        OverlayService.setBubbleVisible(true)
        stopSelf()
        return START_NOT_STICKY
    }
}
