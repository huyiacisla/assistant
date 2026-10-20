package com.huyi.recap.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.huyi.recap.R

/** Thanh dưới + nút "+" tròn giữa (ref_timeline_bottomnav_20-10-2026.png). Pill 4 trang (Note · Recap · Clipboard · Cài đặt) + nút "+" tròn tách riêng bên trái. */

private val NAV = listOf(R.drawable.ic_note, R.drawable.ic_sparkle, R.drawable.ic_clipboard)

@Composable
fun BottomNav(tab: Int, onTab: (Int) -> Unit, onAdd: () -> Unit, backdrop: androidx.compose.ui.graphics.layer.GraphicsLayer, backdropAt: androidx.compose.runtime.State<androidx.compose.ui.geometry.Offset>) {
    val cs = MaterialTheme.colorScheme
    val blur = androidx.compose.ui.graphics.rememberGraphicsLayer().apply { renderEffect = androidx.compose.ui.graphics.BlurEffect(40f, 40f, androidx.compose.ui.graphics.TileMode.Clamp) }
    var pillAt by remember { androidx.compose.runtime.mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    @Composable fun RowScope.slot(i: Int) {
        val on = tab == i
        Box(Modifier.width(48.dp).height(40.dp).clip(CircleShape).background(if (on) cs.primary.copy(alpha = .09f) else Color.Transparent).clickable { onTab(i) }, Alignment.Center) {
            Icon(if (i == 3) Icons.Outlined.Settings else androidx.compose.ui.graphics.vector.ImageVector.vectorResource(id = NAV[i]), null, Modifier.size(22.dp), tint = if (on) cs.primary else cs.onSurfaceVariant.copy(alpha = .6f))
        }
    }
    Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Spacer(Modifier.weight(1f))
        Box(Modifier.size(56.dp).clip(CircleShape).background(cs.primary).clickable(onClick = onAdd), Alignment.Center) {
            Icon(painterResource(R.drawable.ic_plus), "Ghi chú mới", Modifier.size(26.dp), tint = cs.onPrimary)
        }
        Row(Modifier.height(48.dp).clip(CircleShape).androidx_pos { pillAt = it }
            .drawBehind {
                blur.record(size = backdrop.size) { drawLayer(backdrop) }   // kính mờ: blur nền đã vẽ phía sau pill
                translate(backdropAt.value.x - pillAt.x, backdropAt.value.y - pillAt.y) { drawLayer(blur) }
            }.background(cs.surface.copy(alpha = .55f), CircleShape).border(1.dp, cs.outlineVariant, CircleShape).padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            slot(0); slot(1); slot(2); slot(3)
        }
    }
}

private fun Modifier.androidx_pos(f: (androidx.compose.ui.geometry.Offset) -> Unit) = this.onGloballyPositioned { f(it.positionInWindow()) }
