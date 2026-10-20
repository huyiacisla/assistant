package com.huyi.recap.feature.note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.icon.IconLabel
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.core.util.hourOf
import com.huyi.recap.core.util.playAudio
import com.huyi.recap.shared.model.Status
import com.huyi.recap.shared.ui.Pill
import com.huyi.recap.store.Item
import com.huyi.recap.store.state

/** Trạng thái hiển thị + màu tint (chữ đậm, nền cùng màu alpha .12 — đủ tương phản trên trắng). */
private class St(val label: String, val tint: Color)
private fun statusOf(i: Item, now: Long = System.currentTimeMillis()): St? = when {
    i.state == Status.Done -> St("Hoàn thành", Color(0xFF1E8E3E))
    i.state == Status.Ended -> St("Kết thúc", Color(0xFF5F6368))
    i.state == Status.Snoozed -> St("Dời lại", Color(0xFFB26A00))
    i.remindAt == null -> null
    i.remindAt < now -> St("Quá hạn", Color(0xFFC62828))
    else -> St("Chờ nhắc", Color(0xFF0A5FD8))
}
/** " · còn 5g" / " · trễ 2g" quanh giờ nhắc (chỉ khi chưa xong). */
private fun countdown(at: Long, now: Long = System.currentTimeMillis()): String {
    val m = kotlin.math.abs(at - now) / 60000
    val t = if (m >= 1440) "${m / 1440} ngày" else if (m >= 60) "${m / 60} giờ" else "$m phút"
    return if (at >= now) " · còn $t" else " · trễ $t"
}
private val STATUS_MENU = listOf(Status.Done.db to "Hoàn thành", Status.Ended.db to "Kết thúc", Status.Snoozed.db to "Dời 1 giờ", Status.Open.db to "Mở lại")

/** Mục menu hành động: icon trong chấm tint + chữ đậm vừa. */
@Composable
private fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) =
    DropdownMenuItem({ Text(label, color = tint, fontSize = 15.sp, fontWeight = FontWeight.Medium) }, onClick,
        Modifier.padding(horizontal = 6.dp).clip(RoundedCornerShape(14.dp)),
        leadingIcon = { Box(Modifier.size(32.dp).clip(CircleShape).background(tint.copy(alpha = .12f)), Alignment.Center) { Icon(icon, null, Modifier.size(18.dp), tint = tint) } })

private val PIN = listOf(Color(0xFFFF6A3D), Color(0xFF3D7BFF), Color(0xFF8B5CF6))

/**
 * Thẻ note kiểu ghim giấy: khung trắng nghiêng nhẹ + đinh ghim, panel pastel bên trong (màu theo trạng thái, không có = xoay cam/xanh/tím).
 * Nội dung: số thứ tự · giờ tạo · tiêu đề · nội dung · giờ nhắc · nhắc lần n · trạng thái (chạm để đổi).
 */
class SwipeAct(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String, val color: Color, val run: () -> Unit)

/**
 * Thẻ quẹt 2 chiều: right = quẹt phải, left = quẹt trái (null = tắt chiều đó). Hiệu ứng: nền hiện dần theo độ kéo,
 * icon nảy lò xo + rung nhẹ khi qua ngưỡng, thẻ bật lại sau khi chạy (danh sách tự bỏ thẻ khi xoá).
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SwipeCard(frame: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier, right: SwipeAct? = null, left: SwipeAct? = null, content: @Composable () -> Unit) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val settled = androidx.compose.material3.SwipeToDismissBoxValue.Settled
    // state được remember 1 lần ⇒ lambda cũ bị kẹt (vd. đã Xong vẫn gửi "done"); luôn đọc right/left mới nhất
    val curRight by androidx.compose.runtime.rememberUpdatedState(right)
    val curLeft by androidx.compose.runtime.rememberUpdatedState(left)
    val swipe = androidx.compose.material3.rememberSwipeToDismissBoxState(confirmValueChange = {
        when (it) {
            androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd -> curRight?.run?.invoke()
            androidx.compose.material3.SwipeToDismissBoxValue.EndToStart -> curLeft?.run?.invoke()
            else -> {}
        }
        false
    })
    LaunchedEffect(swipe.targetValue) { if (swipe.targetValue != settled) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove) }
    val pop by androidx.compose.animation.core.animateFloatAsState(if (swipe.targetValue != settled) 1.25f else 0.9f, androidx.compose.animation.core.spring(0.5f, 500f), label = "pop")
    val reveal = (swipe.progress * 3f).coerceIn(0f, 1f)
    androidx.compose.material3.SwipeToDismissBox(swipe, modifier = modifier.padding(top = 8.dp, bottom = 12.dp, start = 4.dp, end = 4.dp), enableDismissFromStartToEnd = right != null, enableDismissFromEndToStart = left != null, backgroundContent = {
        // chỉ hiện khi đang quẹt (thẻ nghiêng để lộ viền nếu luôn vẽ)
        val a = when (swipe.dismissDirection) {
            androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd -> right
            androidx.compose.material3.SwipeToDismissBoxValue.EndToStart -> left
            else -> null
        }
        if (a != null) Box(Modifier.fillMaxSize().padding(top = 10.dp).clip(frame).background(a.color.copy(alpha = reveal)), if (a === right) Alignment.CenterStart else Alignment.CenterEnd) {
            Icon(a.icon, a.label, Modifier.padding(horizontal = 24.dp).graphicsLayer { scaleX = pop; scaleY = pop; alpha = reveal }, tint = Color.White)
        }
    }) { content() }
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimelineRow(item: Item, no: Int, onClick: () -> Unit, onDelete: () -> Unit, live: Boolean = false, showDate: Boolean = false, readOnly: Boolean = false, onShare: (() -> Unit)? = null, noStatus: Boolean = false, tapOpens: Boolean = true, modifier: Modifier = Modifier, onStatus: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val st = if (noStatus) null else statusOf(item)
    val title = item.title.ifBlank { item.text.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: (if (item.imagePath != null) "Ảnh" else "(trống)") }
    val rest = item.summary.ifBlank { item.text.lines().dropWhile { it.isBlank() }.drop(1).joinToString(" ").trim() }
    val accent = st?.tint ?: PIN[(no - 1) % PIN.size]
    val frame = RoundedCornerShape(24.dp)
    val panel = RoundedCornerShape(18.dp)
    var menu by remember { mutableStateOf(false) }
    var quick by remember { mutableStateOf(false) }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val src = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val down by src.collectIsPressedAsState()
    // chạm: thẻ co lại + xoay thẳng, đinh lún xuống (lò xo)
    val k by androidx.compose.animation.core.animateFloatAsState(if (down) 1f else 0f, androidx.compose.animation.core.spring(0.55f, 450f), label = "press")
    val canDone = !readOnly && !noStatus
    SwipeCard(frame, modifier,
        right = if (canDone) SwipeAct(Icons.Outlined.Check, if (item.state == Status.Done) "Mở lại" else "Hoàn thành", Color(0xFF2E7D32)) { onStatus(if (item.state == Status.Done) Status.Open.db else Status.Done.db) } else null,   // quẹt phải: hoàn thành (đã xong thì mở lại)
        left = if (!readOnly) SwipeAct(Icons.Outlined.Delete, "Xoá", Tok.Red, onDelete) else null,
    ) {
    Box(Modifier.fillMaxWidth().graphicsLayer {
        rotationZ = (if (no % 2 == 1) -1f else 1f) * (1f - k); scaleX = 1f - .03f * k; scaleY = scaleX
    }) {
        Column(Modifier.padding(top = 10.dp).shadow(8.dp, frame, ambientColor = Color(0x22000000), spotColor = Color(0x33000000)).clip(frame).background(cs.surface).combinedClickable(src, androidx.compose.material3.ripple(), onClick = { if (tapOpens) onClick() }, onLongClick = { haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); quick = true }).padding(horizontal = 8.dp).padding(top = 14.dp, bottom = 8.dp)) {
            DropdownMenu(quick, { quick = false }, shape = RoundedCornerShape(20.dp), containerColor = cs.surface, tonalElevation = 0.dp, shadowElevation = 14.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, cs.outlineVariant)) {
                ActionItem(if (readOnly) Icons.Outlined.Visibility else Icons.Outlined.Edit, if (readOnly) "Xem chi tiết" else "Chỉnh sửa", cs.onSurface) { quick = false; onClick() }
                onShare?.let { ActionItem(Icons.Outlined.Share, "Chia sẻ", cs.onSurface) { quick = false; it() } }
                if (canDone) ActionItem(Icons.Outlined.Check, if (item.state == Status.Done) "Mở lại" else "Hoàn thành", cs.onSurface) { quick = false; onStatus(if (item.state == Status.Done) Status.Open.db else Status.Done.db) }   // thay thế cho cử chỉ quẹt
                if (!readOnly) {
                    HorizontalDivider(Modifier.padding(horizontal = 14.dp, vertical = 2.dp), color = cs.outlineVariant)
                    ActionItem(Icons.Outlined.Delete, "Xoá", Tok.Red) { quick = false; onDelete() }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("%02d".format(no), color = accent, fontSize = 22.sp, fontWeight = FontWeight.Light)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (live) LiveDot()
                    item.audioPath?.takeIf { java.io.File(it).exists() }?.let { ap ->   // loa: phát đoạn ghi âm (các voice đã gộp thành 1 file lúc lưu)
                        var mp by remember(item.id) { mutableStateOf<android.media.MediaPlayer?>(null) }
                        DisposableEffect(item.id) { onDispose { mp?.release() } }
                        Icon(if (mp != null) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp, if (mp != null) "Dừng" else "Phát ghi âm", Modifier.size(28.dp).clip(CircleShape).clickable {   // chạm lại = tắt
                            if (mp != null) { mp?.release(); mp = null }
                            else mp = com.huyi.recap.core.util.playAudio(ap)?.also { m -> m.setOnCompletionListener { it.release(); mp = null } }
                        }.padding(4.dp), tint = accent)
                    }
                    if (showDate) IconLabel(Icons.Outlined.Schedule, "${hourOf(item.createdAt)} · ${dayOf(item.createdAt).let { "%02d/%02d".format(it.dayOfMonth, it.monthValue) }}", cs.onSurface, 13, FontWeight.SemiBold)
                    else Text(hourOf(item.createdAt), color = cs.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Column(Modifier.fillMaxWidth().clip(panel).background(accent.copy(alpha = .12f)).padding(16.dp), Arrangement.spacedBy(6.dp)) {
                Text(title, color = cs.onSurface, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textDecoration = if (item.state == Status.Done && !noStatus) TextDecoration.LineThrough else null)
                if (rest.isNotEmpty()) Text(rest, color = cs.onSurfaceVariant, fontSize = 13.sp, lineHeight = 19.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                item.imagePath?.let { Thumb(it) }
            }
            if ((item.remindAt != null && !noStatus) || st != null || item.tags.isNotBlank())
                FlowRow(Modifier.padding(start = 6.dp, end = 6.dp, top = 10.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val chipBg = cs.surfaceVariant.copy(alpha = .5f)
                    item.tags.split(' ').filter { it.isNotBlank() }.take(3).forEach { Pill("#$it", cs.onSurfaceVariant, chipBg) }
                    if (!noStatus) item.remindAt?.let { Pill("${hourOf(it)}${if (item.state == Status.Open) countdown(it) else ""}", cs.onSecondaryContainer, chipBg, Icons.Outlined.Alarm) }
                    if (item.remindCount > 0 && !noStatus) Pill("Nhắc lần ${item.remindCount}", cs.onSurfaceVariant, chipBg)
                    if (!readOnly) st?.let {
                        Box {
                            Pill(it.label, it.tint, it.tint.copy(alpha = .12f), trailing = Icons.Filled.ArrowDropDown) { menu = true }
                            DropdownMenu(menu, { menu = false }) {
                                STATUS_MENU.forEach { (k, l) -> DropdownMenuItem({ Text(l) }, { menu = false; onStatus(k) }) }
                            }
                        }
                    }
                }
        }
        Box(Modifier.align(Alignment.TopCenter).graphicsLayer { scaleX = 1f - .2f * k; scaleY = scaleX; translationY = 3.dp.toPx() * k }.size(22.dp).shadow(8.dp, CircleShape, spotColor = accent, ambientColor = accent)
            .background(Brush.radialGradient(listOf(Color.White.copy(alpha = .55f), accent), center = Offset(18f, 14f), radius = 40f), CircleShape))
    }
    }
}

@Composable
private fun Thumb(path: String) {
    val bmp by androidx.compose.runtime.produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, path) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            android.graphics.BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 })?.let { it.asImageBitmap() }
        }
    }
    bmp?.let { androidx.compose.foundation.Image(it, null, Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop) }
}
