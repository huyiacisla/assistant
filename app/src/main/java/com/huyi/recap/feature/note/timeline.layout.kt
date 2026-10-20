package com.huyi.recap.feature.note

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.R
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.shared.model.Status
import com.huyi.recap.store.Item
import com.huyi.recap.store.state
import java.time.Instant
import java.time.ZoneId

/** 4 buổi trong ngày; mỗi buổi 1 timeline riêng. Tối gồm cả 00–05h. */
enum class Part(val label: String, val range: String) { Toi("Tối", "18:00 – 05:00"), Chieu("Chiều", "13:00 – 18:00"), Trua("Trưa", "11:00 – 13:00"), Sang("Sáng", "05:00 – 11:00") }

/** Thẻ nằm trong buổi hiện tại của hôm nay → báo sáng. */
fun isLive(ms: Long, now: Long = System.currentTimeMillis()): Boolean {
    val z = ZoneId.systemDefault()
    return java.time.Instant.ofEpochMilli(ms).atZone(z).toLocalDate() == java.time.Instant.ofEpochMilli(now).atZone(z).toLocalDate() && partOf(ms) == partOf(now)
}

/** Chấm nhấp nháy + chữ "Bây giờ". */
@Composable
fun LiveDot(label: String = "Bây giờ") {
    val a by androidx.compose.animation.core.rememberInfiniteTransition(label = "live").animateFloat(.35f, 1f, animationSpec =
        androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(900), androidx.compose.animation.core.RepeatMode.Reverse), label = "a")
    Row(Modifier.clip(CircleShape).background(Tok.Green.copy(alpha = .14f)).padding(horizontal = 8.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).graphicsLayer { alpha = a }.background(Tok.Green, CircleShape))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Tok.Green)
    }
}

fun partOf(ms: Long): Part = when (java.time.Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).hour) {
    in 5..10 -> Part.Sang; in 11..12 -> Part.Trua; in 13..17 -> Part.Chieu; else -> Part.Toi
}

@Composable
fun PartHeader(p: Part, n: Int, open: Boolean = true, live: Boolean = false, onToggle: (() -> Unit)? = null) = GroupHeader(p.label, p.range, n, open, live, onToggle)

@Composable
fun GroupHeader(label: String, sub: String, n: Int, open: Boolean = true, live: Boolean = false, onToggle: (() -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().then(if (onToggle != null) Modifier.clickable(onClick = onToggle) else Modifier).padding(top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onToggle != null) Icon(if (open) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight, if (open) "Thu gọn" else "Mở rộng", Modifier.size(20.dp), tint = cs.onSurfaceVariant)
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        Text("  $sub", fontSize = 12.sp, color = cs.onSurfaceVariant)
        if (live) { Spacer(Modifier.width(8.dp)); LiveDot() }
        Spacer(Modifier.weight(1f))
        Text("$n", fontSize = 12.sp, color = cs.onSurfaceVariant)
    }
}

/** Nhóm trạng thái cho thanh lọc: todo = mở chưa hẹn · wait = có hẹn (chờ/quá hạn/dời) · done = xong/kết thúc. */
fun statGroup(i: Item): String = when {
    i.state == Status.Done || i.state == Status.Ended -> "done"
    i.state == Status.Snoozed || i.remindAt != null -> "wait"
    else -> "todo"
}
private val STAT_TABS = listOf(Triple("todo", "Cần làm", R.drawable.ic_todo), Triple("done", "Hoàn thành", R.drawable.ic_check), Triple("wait", "Chờ nhắc", R.drawable.ic_clock))

/** Segmented: khay trắng bo tròn, mục chọn = pill đen; chạm lại mục đang chọn = bỏ lọc. */
@Composable
fun StatusTabs(sel: String?, count: (String) -> Int, modifier: Modifier = Modifier, onSel: (String?) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().clip(CircleShape).background(cs.surface).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        STAT_TABS.forEach { (k, label, icon) ->
            val on = sel == k
            val c = if (on) cs.onPrimary else cs.onSurfaceVariant
            Row(
                Modifier.weight(if (on) 1.25f else 1f).clip(CircleShape).background(if (on) cs.primary else Color.Transparent)
                    .clickable { onSel(if (on) null else k) }.padding(vertical = 11.dp),
                Arrangement.Center, Alignment.CenterVertically,
            ) {
                Icon(painterResource(icon), null, Modifier.size(16.dp), tint = c)
                Text(" $label ${count(k)}", fontSize = 13.sp, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal, color = c, maxLines = 1)
            }
        }
    }
}
