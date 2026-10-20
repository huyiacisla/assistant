package com.huyi.recap.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.util.dow
import java.time.DayOfWeek
import java.time.LocalDate

/** Tuần chứa `sel`, bắt đầu thứ 2; ‹ › đổi tuần (không vượt tuần hiện tại). Chấm dưới ngày = có ghi chú. */
@Composable
fun DayStrip(sel: LocalDate, withNotes: Set<LocalDate>, allowFuture: Boolean = true, onSel: (LocalDate) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mon = sel.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val thisMon = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val sun = mon.plusDays(6)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹", Modifier.clip(CircleShape).clickable { onSel(sel.minusWeeks(1)) }.padding(horizontal = 14.dp, vertical = 2.dp), fontSize = 20.sp, color = cs.onSurfaceVariant)
            Text(if (mon == thisMon) "Tuần này" else "${mon.dayOfMonth}/${mon.monthValue} – ${sun.dayOfMonth}/${sun.monthValue}", Modifier.weight(1f), fontSize = 12.sp,
                color = cs.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            val canNext = allowFuture || mon.plusWeeks(1) <= LocalDate.now()
            Text("›", Modifier.clip(CircleShape).clickable(enabled = canNext) { onSel(minOf(sel.plusWeeks(1), if (allowFuture) sel.plusWeeks(1) else LocalDate.now())) }.padding(horizontal = 14.dp, vertical = 2.dp),
                fontSize = 20.sp, color = cs.onSurfaceVariant.copy(alpha = if (canNext) 1f else .3f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (0L..6L).map { mon.plusDays(it) }.forEach { d ->
                val on = d == sel
                val future = d.isAfter(LocalDate.now())
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(if (on) cs.primary else Color.Transparent).clickable(enabled = allowFuture || !future) { onSel(d) }.padding(vertical = 10.dp),
                    Arrangement.spacedBy(4.dp), Alignment.CenterHorizontally,
                ) {
                    val c = (if (on) cs.onPrimary else cs.onSurfaceVariant).copy(alpha = if (future) (if (allowFuture) .6f else .3f) else 1f)
                    Text("${d.dayOfMonth}", fontSize = 13.sp, color = c, fontWeight = FontWeight.Medium)
                    Text(dow(d), fontSize = 13.sp, color = c, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (d in withNotes) (if (on) cs.onPrimary else cs.primary) else Color.Transparent))
                }
            }
        }
    }
}
