package com.huyi.recap.feature.quicknote

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.R
import com.huyi.recap.core.icon.IconLabel
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.feature.ai.KINDS
import com.huyi.recap.feature.remind.remindLabel
import com.huyi.recap.shared.ui.Chip
import com.huyi.recap.ui.IconButton
import kotlinx.coroutines.launch

@Composable
internal fun QuickNoteActivity.Sheet() {
    val cs = MaterialTheme.colorScheme
    val kb = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    val scroll = rememberScrollState()
    LaunchedEffect(kb, asked, done) { if (kb) scroll.animateScrollTo(scroll.maxValue) }   // bàn phím mở / panel AI dài ra → cuộn xuống để ✓ không bị che
    LaunchedEffect(text) { warned = false; recap = null; aiAt = null; aiVeto = false; analysis = null; brief = null; done = 0; asked = false }
    Box(Modifier.fillMaxSize().background(Tok.Scrim).clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { leave() },
        contentAlignment = Alignment.BottomCenter) {
        Column(Modifier.fillMaxWidth().imePadding().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(cs.background)
            .clickable(enabled = false) {}.verticalScroll(scroll).navigationBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (!kb) Box(Modifier.size(36.dp, 5.dp).clip(CircleShape).background(Tok.Handle))
            // Số giây đang nghe (trái) + ✕ (phải); nút ghi âm = đinh ghim giữa thẻ note
            var secs by remember { mutableStateOf(0) }
            LaunchedEffect(listening) { while (listening) { secs = ((android.os.SystemClock.elapsedRealtime() - t0) / 1000).toInt(); kotlinx.coroutines.delay(250) } }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {   // giờ nghe trái · ✕ phải; mic nằm ở đinh ghim thẻ note
                if (listening) Text("%d:%02d".format(secs / 60, secs % 60), fontSize = 22.sp, fontWeight = FontWeight.Medium, color = Tok.Blue)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(32.dp).clip(CircleShape).background(cs.onBackground.copy(alpha = .05f)).clickable { leave() }, Alignment.Center) { Icon(Icons.Filled.Close, "Đóng", Modifier.size(18.dp)) }   // ✕ cùng hàng số giây: không tốn hàng trống khi bàn phím mở
            }
            if (!kb) status?.let { Text(it, fontSize = 13.sp, color = cs.onSurfaceVariant) }
            if (asked) Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Tok.Blue.copy(alpha = .12f)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val at = remindAt(text)
                val a = analysis
                fun line(i: Int, label: String, v: String?) = when {
                    done and (1 shl (i - 1)) == 0 -> Icons.Outlined.HourglassTop to "$label…"
                    v == null -> Icons.Outlined.ErrorOutline to "$label: không có (lỗi mạng? bấm ✦ thử lại)"
                    else -> Icons.Filled.CheckCircle to "$label: $v"
                }
                listOf(
                    line(1, "Hiểu", recap),
                    line(2, "Giờ nhắc", at?.let(::remindLabel) ?: "không đặt".takeIf { done and 2 != 0 }),
                    line(3, "Loại", a?.let { (KINDS[it.kind] ?: it.kind) + it.tags.joinToString("") { t -> " #$t" } }),
                    line(4, "Tiêu đề", brief?.let { it.title + if (it.summary.isNotEmpty()) " — " + it.summary else "" }),
                ).forEach { (ic, t) -> IconLabel(ic, t, cs.onSurface, 13) }
                recap?.let { r -> Chip("Dùng bản hiểu để sửa", icon = Icons.Outlined.Edit) { text = r; editing = true } }   // đưa kết quả AI vào ô chữ để chỉnh
            }
            // Text = phụ, chỉ hiện khi đã có chữ hoặc chọn gõ tay
            val accent = androidx.compose.ui.graphics.Color(0xFF3D7BFF)   // cùng bảng PIN với thẻ Timeline
            val frame = RoundedCornerShape(24.dp)
            Box(Modifier.fillMaxWidth()) {   // thẻ note đang soạn: giống TimelineRow (khung 24dp, bóng, đinh ghim)
            Column(Modifier.fillMaxWidth().padding(top = 22.dp).shadow(8.dp, frame, ambientColor = androidx.compose.ui.graphics.Color(0x22000000), spotColor = androidx.compose.ui.graphics.Color(0x33000000))
                .clip(frame).background(cs.surface).padding(horizontal = 14.dp).padding(top = 26.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val focus = remember { androidx.compose.ui.focus.FocusRequester() }
                var editPrev by remember { mutableStateOf(listOf<PcmClip>()) }
                val startEdit = { editPrev = clips.toList(); editing = true; focus.requestFocus(); Unit }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Alarm, null, Modifier.size(16.dp), tint = cs.onSurfaceVariant)
                    Chip(chosen?.let(::remindLabel) ?: "Ngày giờ", chosen != null, Icons.Outlined.CalendarMonth) { pickDateTime() }
                    if (chosen != null) Chip("", icon = Icons.Filled.Close) { chosen = null; pick = null }
                    Spacer(Modifier.weight(1f))
                    if (text.isNotEmpty()) Chip("Xoá hết chữ", icon = Icons.Filled.Close) { for (i in clips.indices) clips[i] = PcmClip("", clips[i].pcm) }
                    val fm =androidx.compose.ui.platform.LocalFocusManager.current
                    IconButton({ if (editing) { clips.clear(); clips.addAll(editPrev); editing = false; fm.clearFocus() } else startEdit() }, Modifier.size(28.dp)) {   // bút → huỷ: huỷ = về chữ trước khi sửa + tắt bàn phím
                        Icon(if (editing) Icons.Filled.Close else Icons.Outlined.Edit, if (editing) "Huỷ sửa" else "Sửa", Modifier.size(16.dp)) }
                }
                if (transcribing) IconLabel(Icons.Outlined.HourglassTop, "Đang chép lời…", cs.onSurfaceVariant, 13)
                // mỗi clip 1 dòng độc lập: sửa chữ riêng (khi bật bút) + ✕ xoá clip (cả chữ lẫn tiếng)
                val rows = if (clips.isEmpty()) listOf(PcmClip("", ByteArray(0))) else clips.toList()
                val ps = rememberScrollState()
                LaunchedEffect(text) { ps.scrollTo(ps.maxValue) }   // chữ mới nằm cuối → luôn thấy con trỏ
                Column(Modifier.fillMaxWidth().heightIn(max = if (kb) 150.dp else 260.dp).clip(RoundedCornerShape(18.dp)).background(accent.copy(alpha = .12f)).padding(12.dp).verticalScroll(ps), verticalArrangement = Arrangement.spacedBy(8.dp)) {   // nhiều clip → cuộn trong ô
                rows.forEachIndexed { i, c ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surface).clickable(enabled = !editing) { startEdit() }   // chạm ô chữ = bật sửa
                        .border(if (editing) 1.5.dp else 0.dp, if (editing) androidx.compose.ui.graphics.Color(0xFFFF6A3D) else androidx.compose.ui.graphics.Color.Transparent, RoundedCornerShape(14.dp)).padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),   // mỗi clip = 1 khu vực riêng
                        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        BasicTextField(c.text, { v -> if (i < clips.size) clips[i] = PcmClip(v, c.pcm) else clips.add(PcmClip(v, c.pcm)) },
                            Modifier.weight(1f).heightIn(min = 36.dp).padding(vertical = 4.dp).then(if (i == 0) Modifier.focusRequester(focus) else Modifier), readOnly = !editing, cursorBrush = SolidColor(cs.onSurface),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 22.sp, color = cs.onSurface))
                        if (i < clips.size) {
                            Icon(Icons.Filled.Close, "Xoá câu", Modifier.padding(top = 4.dp).size(28.dp).clip(CircleShape).clickable { clips.removeAt(i); rewriteAudio() }.padding(4.dp), tint = cs.onSurfaceVariant)
                            if (c.pcm.isNotEmpty()) Icon(if (playing == i) Icons.Filled.Stop else Icons.Filled.PlayArrow, "Phát lại", Modifier.padding(top = 4.dp).size(28.dp).clip(CircleShape).clickable {   // phát riêng clip này (WAV tạm trong cache)
                                val f = java.io.File(cacheDir, "clip.wav")
                                f.outputStream().use { it.write(wavHeader(c.pcm.size)); it.write(c.pcm) }; play(f.absolutePath, i)
                            }.padding(4.dp), tint = Tok.Blue)
                        }
                    }
                    if (i < clips.size - 1) Row(Modifier.align(Alignment.CenterHorizontally).clip(CircleShape).clickable {   // gộp clip i + i+1: nối chữ + nối tiếng
                        val n = clips[i + 1]; clips[i] = PcmClip(listOf(c.text, n.text).filter { it.isNotBlank() }.joinToString(" "), c.pcm + n.pcm); clips.removeAt(i + 1); rewriteAudio()
                    }.padding(horizontal = 10.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Add, null, Modifier.size(14.dp), tint = Tok.Blue); Text("Gộp", fontSize = 12.sp, color = Tok.Blue)
                    }
                }
                }
                img?.let { p ->
                    val bmp = remember(p) { android.graphics.BitmapFactory.decodeFile(p, android.graphics.BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() }
                    Box { bmp?.let { Image(it, null, Modifier.fillMaxWidth().heightIn(max = 160.dp).clip(RoundedCornerShape(14.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop) }
                        Icon(Icons.Filled.Close, "Bỏ ảnh", Modifier.align(Alignment.TopEnd).padding(6.dp).clip(CircleShape).background(Tok.Scrim).clickable { java.io.File(p).delete(); img = null }.padding(4.dp).size(16.dp), tint = androidx.compose.ui.graphics.Color.White) }
                }
                val at = remember(text, aiAt, aiVeto, pick, dismissed) { remindAt(text) }
                if (at != null) Row(Modifier.clip(CircleShape).background(Tok.Blue.copy(alpha = .12f)).padding(start = 10.dp, top = 4.dp, bottom = 4.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconLabel(Icons.Outlined.Alarm, "Sẽ nhắc lúc ${remindLabel(at)}", Tok.Blue, 12, FontWeight.Medium)
                    Icon(Icons.Filled.Close, "Xoá giờ nhắc", Modifier.size(16.dp).clip(CircleShape).clickable { dismissed = true; pick = null }, tint = Tok.Blue)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Chip("", asked, Icons.Outlined.AutoAwesome) { analyze() }   // AI chỉ chạy khi bấm (chữ đã xong); không đụng chữ đã sửa
                    Chip("", icon = Icons.Filled.Add) { askImg.launch("image/*") }
                    Spacer(Modifier.weight(1f))
                    Chip("", text.isNotBlank() || img != null || audio != null, Icons.Filled.Done) { save() }   // save() tự bỏ qua khi rỗng
                }
            }
            val pinC = if (listening) Tok.Red else accent
            val act = audio.takeIf { !listening }   // 3 nút tiếng đã ghi: chỉ khi có audio và không đang ghi
            val btn = Modifier.size(36.dp)   // IconButton đã có nền tròn tonal riêng
            Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {   // [nghe lại][nghe kỹ] · mic (đinh ghim, giữa) · [xoá]
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
                    act?.let { p ->
                        IconButton({ play(p, -1) }, btn) { Icon(if (playing == -1) Icons.Filled.Stop else Icons.Filled.PlayArrow, if (playing == -1) "Dừng" else "Nghe lại", Modifier.size(18.dp)) }
                        if (!transcribing) IconButton({ carefully() }, btn) { Icon(Icons.Outlined.Hearing, "Nghe kỹ", Modifier.size(18.dp)) }
                    }
                }
                Box(Modifier.size(44.dp), Alignment.Center) {   // nút ghi âm: chạm bắt đầu/dừng; đang ghi → đỏ + gợn sóng
                if (listening) {
                    val tr = androidx.compose.animation.core.rememberInfiniteTransition(label = "ripple")
                    (0..2).forEach { i ->
                        val p by tr.animateFloat(0f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(900, easing = androidx.compose.animation.core.FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Restart, androidx.compose.animation.core.StartOffset(i * 300)), label = "r$i")
                        Box(Modifier.fillMaxSize().graphicsLayer { val s = 1f + .9f * p; scaleX = s; scaleY = s; alpha = .45f * (1f - p) }.clip(CircleShape).background(pinC))
                    }
                }
                Box(Modifier.fillMaxSize().shadow(8.dp, CircleShape, spotColor = pinC, ambientColor = pinC).background(pinC, CircleShape).clip(CircleShape).pointerInput(Unit) { detectTapGestures(onTap = { if (rec == null) startRec() else stopRec() }) }, Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_mic), if (listening) "Dừng ghi âm" else "Ghi âm", Modifier.size(22.dp), tint = androidx.compose.ui.graphics.Color.White)
                }
                }
                Row(Modifier.weight(1f)) {
                    act?.let { IconButton({ dropAudio() }, btn) { Icon(Icons.Filled.Close, "Xoá ghi âm", Modifier.size(18.dp)) } }
                }
            }
            }
        }
    }
}

/** Bước 1 chọn ngày (M3 DatePicker) → bước 2 chọn giờ (M3 TimePicker, 24h). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun QuickNoteActivity.DateTimeDialog() {
    val base = java.util.Calendar.getInstance().apply { chosen?.let { timeInMillis = it } }
    val dp = rememberDatePickerState(initialSelectedDateMillis = java.time.LocalDate.of(base.get(1), base.get(2) + 1, base.get(5)).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
    val tp = rememberTimePickerState(base.get(java.util.Calendar.HOUR_OF_DAY), base.get(java.util.Calendar.MINUTE), true)
    var step by remember { mutableStateOf(0) }
    fun done() {
        val d = java.time.Instant.ofEpochMilli(dp.selectedDateMillis ?: return).atZone(java.time.ZoneOffset.UTC).toLocalDate()
        val ms = d.atTime(tp.hour, tp.minute).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        chosen = ms; pick = if (ms > System.currentTimeMillis()) ms else null; dismissed = false; picking = false
    }
    if (step == 0) DatePickerDialog(
        onDismissRequest = { picking = false },
        confirmButton = { TextButton(onClick = { if (dp.selectedDateMillis != null) step = 1 }) { Text("Tiếp") } },
        dismissButton = { IconButton(onClick = { picking = false }) { Icon(Icons.Filled.Close, "Huỷ") } },
    ) { DatePicker(dp) }
    else AlertDialog(
        onDismissRequest = { picking = false },
        confirmButton = { TextButton(onClick = ::done) { Text("Xong") } },
        dismissButton = { IconButton(onClick = { step = 0 }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
        text = { TimePicker(tp) },
    )
}

@Composable
private fun Chip(label: String, on: Boolean = false, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, click: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val c = if (on) cs.onPrimary else cs.onSurface
    Row(Modifier.clip(CircleShape).background(if (on) cs.primary else cs.surface).border(1.dp, cs.outlineVariant, CircleShape).clickable(onClick = click).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        icon?.let { Icon(it, null, Modifier.size(14.dp), tint = c) }
        if (label.isNotEmpty()) Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = c)
    }
}
