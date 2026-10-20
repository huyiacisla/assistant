package com.huyi.recap.overlay

import android.animation.ValueAnimator
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.huyi.recap.R

internal fun OverlayService.bubbleParams() = WindowManager.LayoutParams(
    size, size,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    PixelFormat.TRANSLUCENT,
).apply { gravity = Gravity.TOP or Gravity.START; x = 0; y = (200 * dp).toInt() }

internal fun OverlayService.addBubble() {
    val lp = bubbleLp
    val v = android.widget.ImageView(this).apply {
        setImageResource(R.drawable.ic_launcher_fg); setColorFilter(0xE6000000.toInt()); scaleType = android.widget.ImageView.ScaleType.FIT_CENTER   // cùng icon app (foreground launcher, có sẵn vùng đệm)   // icon tối trên nền kính sáng
        background = android.graphics.drawable.LayerDrawable(arrayOf(
            GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0x33000000) }, // tint tối nhẹ: nổi trên nền sáng
            GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0x99FFFFFF.toInt(), 0x33FFFFFF)).apply { // kính: trắng mờ + viền sáng
                shape = GradientDrawable.OVAL; setStroke((2 * dp).toInt(), 0xCCFFFFFF.toInt())
            },
            GradientDrawable().apply { // highlight 30%/30%
                shape = GradientDrawable.OVAL; gradientType = GradientDrawable.RADIAL_GRADIENT
                setGradientCenter(0.3f, 0.3f); gradientRadius = size * 0.6f; colors = intArrayOf(0x66FFFFFF, 0x00FFFFFF)
            },
        ))
    }

    var downX = 0f; var downY = 0f; var startX = 0; var startY = 0; var dragged = false
    v.setOnTouchListener { _, e ->
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = e.rawX; downY = e.rawY; startX = lp.x; startY = lp.y; dragged = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.rawX - downX; val dy = e.rawY - downY
                if (dragged || dx * dx + dy * dy > 16 * 16) { // ngưỡng kéo ~16px
                    if (!dragged) closeMenu()
                    dragged = true
                    lp.x = startX + dx.toInt(); lp.y = startY + dy.toInt()
                    wm.updateViewLayout(v, lp)
                }
            }
            MotionEvent.ACTION_UP -> if (dragged) snapToEdge(v, lp) else toggleMenu()
        }
        true
    }
    wm.addView(v, lp)
    bubble = v
}

internal fun OverlayService.bounds() = wm.currentWindowMetrics.bounds

internal fun OverlayService.snapToEdge(v: View, lp: WindowManager.LayoutParams) {
    val b = bounds()
    val toX = if (lp.x + size / 2 < b.width() / 2) 0 else b.width() - size
    val toY = lp.y.coerceIn(0, b.height() - size)
    val fromX = lp.x; val fromY = lp.y
    ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 150
        addUpdateListener {
            val t = it.animatedValue as Float
            lp.x = (fromX + (toX - fromX) * t).toInt()
            lp.y = (fromY + (toY - fromY) * t).toInt()
            wm.updateViewLayout(v, lp)
        }
        start()
    }
}
