package com.huyi.recap.overlay

import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.toArgb
import com.huyi.recap.R
import com.huyi.recap.app.MainActivity
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.feature.capture.CaptureActivity
import com.huyi.recap.feature.capture.ShotAccessibilityService
import com.huyi.recap.feature.quicknote.QuickNoteActivity

/** Nhấn: scale 0.96 · 120ms (DESIGN.md §6). */
internal fun View.pressable() = setOnTouchListener { v, e ->
    when (e.action) {
        android.view.MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(120).start()
        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
    }
    false
}

internal fun OverlayService.openMenu() {
    val a11y = { ShotAccessibilityService.instance }
    // (nhãn, icon, màu, hành động) — hiển thị dạng list [icon] text
    val items = listOf<MenuItem>(
        MenuItem("Chụp & Lưu", R.drawable.ic_camera, Tok.Red.toArgb()) {
            val a = a11y()
            if (a != null) { OverlayService.setBubbleVisible(false); Handler(Looper.getMainLooper()).postDelayed({ a.shoot() }, 250) }
            else startActivity(Intent(this, CaptureActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        },
        MenuItem("Ghi chú nhanh", R.drawable.ic_mic, Tok.Blue.toArgb()) { startActivity(Intent(this, QuickNoteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
        MenuItem("Sao chép", R.drawable.ic_clipboard, Tok.Green.toArgb()) { startActivity(Intent(this, MainActivity::class.java).putExtra("go", "clip").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) },
        MenuItem("Recap", R.drawable.ic_sparkle, Tok.Purple.toArgb()) { startActivity(Intent(this, MainActivity::class.java).putExtra("go", "cards").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) },
        MenuItem("Ghi chú", R.drawable.ic_note, Tok.Orange.toArgb()) { startActivity(Intent(this, MainActivity::class.java).putExtra("go", "note").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)) },
    )
    fun px(v: Int) = (v * dp).toInt()
    val b = bounds()
    val rowH = px(52); val panelW = px(224); val pad = px(8)
    val panelH = rowH * (items.size + 1) + pad * 2   // +1: hàng Đóng
    val onLeft = bubbleLp.x + size / 2 < b.width() / 2

    val root = android.widget.FrameLayout(this).apply { setOnClickListener { closeMenu() } }

    // hàng [icon] text: icon trong vòng tròn pastel 36dp + nhãn
    fun row(icon: Int, color: Int, text: String, onClick: () -> Unit) = android.widget.LinearLayout(this).apply {
        orientation = android.widget.LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        setPadding(px(8), 0, px(16), 0)
        background = GradientDrawable().apply { cornerRadius = 18 * dp; setColor(Color.TRANSPARENT) }
        addView(android.widget.FrameLayout(context).apply {
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.argb(0x26, Color.red(color), Color.green(color), Color.blue(color))) }
            addView(android.widget.ImageView(context).apply { setImageResource(icon); setColorFilter(color) }, android.widget.FrameLayout.LayoutParams(px(20), px(20), Gravity.CENTER))
        }, android.widget.LinearLayout.LayoutParams(px(36), px(36)))
        addView(TextView(context).apply { this.text = text; setTextColor(0xFF111111.toInt()); textSize = 15f; typeface = android.graphics.Typeface.DEFAULT_BOLD; maxLines = 1 },
            android.widget.LinearLayout.LayoutParams(-2, -2).apply { leftMargin = px(12) })
        setOnClickListener { onClick() }; pressable()   // act trước closeMenu: còn overlay hiện thì mới được mở Activity từ nền
    }

    val panel = android.widget.LinearLayout(this).apply {
        orientation = android.widget.LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad)
        background = GradientDrawable().apply { cornerRadius = 26 * dp; setColor(0xE6FFFFFF.toInt()); setStroke(px(1), 0x99FFFFFF.toInt()) }   // kính: trắng 90% + viền sáng
        elevation = 12 * dp
        pivotX = if (onLeft) 0f else panelW.toFloat(); pivotY = panelH / 2f
    }
    items.forEach { panel.addView(row(it.icon, it.color, it.label) { it.act(); closeMenu() }, android.widget.LinearLayout.LayoutParams(-1, rowH)) }
    // Đóng: chạm = đóng, giữ = tắt nút nổi
    panel.addView(row(R.drawable.ic_close, Tok.Ink2.toArgb(), "Đóng") { closeMenu() }.apply { setOnLongClickListener { closeMenu(); stopSelf(); true } }, android.widget.LinearLayout.LayoutParams(-1, rowH))

    // bung ra từ phía nút nổi
    panel.alpha = 0f; panel.scaleX = .85f; panel.scaleY = .85f
    panel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(180).setInterpolator(android.view.animation.DecelerateInterpolator()).start()
    for (i in 0 until panel.childCount) panel.getChildAt(i).apply {
        alpha = 0f; translationX = (if (onLeft) -px(12) else px(12)).toFloat()
        animate().alpha(1f).translationX(0f).setStartDelay(i * 20L).setDuration(180).setInterpolator(android.view.animation.DecelerateInterpolator()).start()
    }

    val gap = px(10)
    val left = if (onLeft) bubbleLp.x + size + gap else bubbleLp.x - gap - panelW
    val top = (bubbleLp.y + size / 2 - panelH / 2).coerceIn(px(24), (b.height() - panelH - px(48)).coerceAtLeast(px(24)))
    root.addView(panel, android.widget.FrameLayout.LayoutParams(panelW, panelH).apply { leftMargin = left.coerceIn(px(8), b.width() - panelW - px(8)); topMargin = top })

    val lp = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT,
    )
    bubble?.visibility = View.INVISIBLE
    wm.addView(root, lp)
    menu = root
}

internal class MenuItem(val label: String, val icon: Int, val color: Int, val act: () -> Unit)
