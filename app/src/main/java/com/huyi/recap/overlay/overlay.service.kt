package com.huyi.recap.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import com.huyi.recap.R
import com.huyi.recap.app.MainActivity
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.feature.clipboard.ClipboardActivity

/** Nút nổi kéo thả + menu. View thường (không ComposeView) để tránh lỗi lifecycle trong WindowManager. */
class OverlayService : Service() {
    internal lateinit var wm: WindowManager
    internal var bubble: View? = null
    internal var menu: View? = null
    internal var logThread: Thread? = null
    internal val bubbleLp by lazy { bubbleParams() }
    internal val dp by lazy { resources.displayMetrics.density }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startForeground(1, notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        addBubble()
        watchClipboard()
    }

    /**
     * Android 10+ không báo copy cho app nền, nhưng ClipboardService ghi log "Denying clipboard access to <pkg>"
     * cho mỗi listener bị từ chối. Đăng ký 1 listener rỗng rồi đọc log (cần READ_LOGS qua adb) ⇒ biết mọi lần copy.
     * ponytail: phụ thuộc chuỗi log của ROM; hỏng thì dùng menu "Lưu clipboard".
     */
    internal fun watchClipboard() {
        (getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).addPrimaryClipChangedListener { }
        val me = "Denying clipboard access to $packageName"
        logThread = Thread {
            runCatching {
                val p = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "brief", "-T", "1", "ClipboardService:E", "*:S"))
                var last = 0L
                p.inputStream.bufferedReader().forEachLine { l ->
                    val now = System.currentTimeMillis()
                    if (l.contains(me) && now - last > 800) {
                        last = now
                        android.util.Log.i("Recap", "copy phát hiện qua log")
                        Handler(Looper.getMainLooper()).post {
                            startActivity(Intent(this, ClipboardActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION))
                        }
                    }
                }
            }.onFailure { android.util.Log.w("Recap", "logcat watcher lỗi: $it") }
        }.also { it.isDaemon = true; it.start() }
    }

    override fun onDestroy() {
        instance = null
        closeMenu()
        bubble?.let { wm.removeView(it) }
        super.onDestroy()
    }

    internal fun notification(): Notification {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel("overlay", "Nút nổi", NotificationManager.IMPORTANCE_MIN)
        )
        return Notification.Builder(this, "overlay")
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentTitle("Recap đang chạy")
            .build()
    }

    internal val size get() = (40 * dp).toInt()

    internal fun toggleMenu() = if (menu == null) openMenu() else closeMenu()

    internal fun closeMenu() {
        menu?.let { wm.removeView(it) }
        menu = null
        bubble?.visibility = View.VISIBLE
    }

    companion object {
        private var instance: OverlayService? = null
        val running get() = instance != null
        /** Ẩn bubble khi chụp để không lọt vào ảnh. */
        /** Snackbar nổi 5s với nút Hoàn tác. */
        fun showUndo(msg: String, sub: String = "", thumb: android.graphics.Bitmap? = null, onUndo: () -> Unit) = instance?.undoBar(msg, sub, thumb, onUndo)

        /** Card tiến trình các bước; trả hàm cập nhật: n = số bước đã xong (0..size), -1 = đóng. */
        fun showSteps(labels: List<String>): (Int) -> Unit = instance?.stepsCard(labels) ?: { }

        fun setBubbleVisible(v: Boolean) {
            instance?.bubble?.visibility = if (v) View.VISIBLE else View.INVISIBLE
        }
    }

    /** Bottom sheet kết quả (kính trắng, bo 22): tiêu đề, dòng phụ, thumbnail, HOÀN TÁC + Xem card đã lưu. Tự đóng sau 5s. */
    internal fun undoBar(msg: String, sub: String, thumb: android.graphics.Bitmap?, onUndo: () -> Unit) {
        fun px(v: Int) = (v * dp).toInt()
        val h = Handler(Looper.getMainLooper())
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(16), px(10), px(16), px(14))
            background = GradientDrawable().apply { cornerRadius = 22 * dp; setColor(Tok.Sheet.toArgb()); setStroke(px(1), Tok.LineStrong.toArgb()) }
            elevation = 12 * dp
        }
        card.addView(View(this).apply { background = GradientDrawable().apply { cornerRadius = 3 * dp; setColor(Tok.Handle.toArgb()) } },
            LinearLayout.LayoutParams(px(36), px(5)).apply { gravity = Gravity.CENTER_HORIZONTAL; bottomMargin = px(10) })
        val head = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        thumb?.let { head.addView(android.widget.ImageView(this).apply { setImageBitmap(it); clipToOutline = true
            outlineProvider = object : android.view.ViewOutlineProvider() { override fun getOutline(v: View, o: android.graphics.Outline) = o.setRoundRect(0, 0, v.width, v.height, 8 * dp) } },
            LinearLayout.LayoutParams(px(48), px(64)).apply { rightMargin = px(12) }) }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        col.addView(TextView(this).apply { text = msg; setTextColor(Color.BLACK); textSize = 16f; typeface = android.graphics.Typeface.DEFAULT_BOLD })
        if (sub.isNotBlank()) col.addView(TextView(this).apply { text = sub; setTextColor(Tok.Ink2.toArgb()); textSize = 12f })
        head.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.addView(head)
        val close = { card.parent?.let { wm.removeView(card) } }
        fun btn(label: String, fill: Int, ink: Int, act: () -> Unit) = TextView(this).apply {
            text = label; setTextColor(ink); textSize = 13f; typeface = android.graphics.Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
            background = GradientDrawable().apply { cornerRadius = 20 * dp; setColor(fill) }
            setOnClickListener { act(); close() }; pressable() // act trước: overlay còn hiện thì mới được phép mở Activity từ nền
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(btn("Hoàn tác", Tok.Bg.toArgb(), Color.BLACK) { onUndo(); Toast.makeText(this@OverlayService, "Đã hoàn tác", Toast.LENGTH_SHORT).show() },
            LinearLayout.LayoutParams(0, px(40), 1f).apply { rightMargin = px(8) })
        row.addView(btn("Xem card đã lưu", Color.BLACK, Color.WHITE) { startActivity(Intent(this, MainActivity::class.java).putExtra("go", "cards").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) },
            LinearLayout.LayoutParams(0, px(40), 1f))
        card.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = px(12) })
        wm.addView(card, WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels - px(24)), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = px(24) })
        h.postDelayed({ close() }, 5000)
    }

    internal fun stepsCard(labels: List<String>): (Int) -> Unit {
        fun px(v: Int) = (v * dp).toInt()
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(px(18), px(14), px(18), px(14))
            background = GradientDrawable().apply { cornerRadius = 22 * dp; setColor(Tok.Sheet.toArgb()); setStroke(px(1), Tok.LineStrong.toArgb()) }
            elevation = 12 * dp
        }
        val rows = labels.map { TextView(this).apply { textSize = 14f; setPadding(0, px(3), 0, px(3)) }.also { card.addView(it) } }
        val update = { n: Int ->
            if (n < 0) { if (card.parent != null) wm.removeView(card) }
            else rows.forEachIndexed { i, t ->
                t.text = (if (i < n) "✓  " else if (i == n) "…  " else "○  ") + labels[i]
                t.setTextColor(if (i <= n) Color.BLACK else Tok.Ink2.toArgb())
                t.typeface = if (i == n) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            }
        }
        update(0)
        wm.addView(card, WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels - px(24)), WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT,
        ).apply { gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL; y = px(24) })
        Handler(Looper.getMainLooper()).postDelayed({ update(-1) }, 20000) // an toàn: tự gỡ nếu luồng kẹt
        return update
    }

    internal fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
