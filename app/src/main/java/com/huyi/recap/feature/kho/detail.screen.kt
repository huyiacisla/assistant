package com.huyi.recap.feature.kho

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.icon.IconLabel
import com.huyi.recap.core.theme.BeVietnam
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.core.theme.blueInk
import com.huyi.recap.core.util.dayLabel
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.core.util.hourOf
import com.huyi.recap.feature.remind.cancelRemind
import com.huyi.recap.feature.remind.remindLabel
import com.huyi.recap.feature.remind.scheduleRemind
import com.huyi.recap.store.Folder
import com.huyi.recap.store.Item
import com.huyi.recap.ui.IconButton
import com.huyi.recap.ui.IconLabelChip
import com.huyi.recap.ui.Markdown
import com.huyi.recap.ui.TagPicker
import com.huyi.recap.ui.linePrefix
import com.huyi.recap.ui.mdTransform
import com.huyi.recap.ui.toggleTaskAt
import com.huyi.recap.ui.wrap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun DetailScreen(item: Item, folders: List<Folder>, knownTags: List<String>, onSave: (Item) -> Unit, onDelete: () -> Unit, onBack: () -> Unit) {
    // rememberSaveable: sống qua xoay máy / process bị giết; TextFieldValue.Saver giữ cả vùng chọn để nút B/I/H chèn đúng chỗ
    var tf by androidx.compose.runtime.saveable.rememberSaveable(stateSaver = androidx.compose.ui.text.input.TextFieldValue.Saver) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(item.text)) }
    val text = tf.text
    var tags by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(item.tags) }
    var img by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(item.imagePath) }   // thêm/đổi/bỏ ảnh; file ảnh cũ xoá khi autosave ghi nhận ảnh mới
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val pickImg = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        runCatching {
            val f = java.io.File(ctx.filesDir, "img_${System.currentTimeMillis()}.jpg")
            ctx.contentResolver.openInputStream(uri)!!.use { i -> f.outputStream().use { i.copyTo(it) } }
            img = f.absolutePath
        }.onFailure { android.widget.Toast.makeText(ctx, "Không thêm được ảnh: ${it.message ?: "lỗi đọc tệp"}", android.widget.Toast.LENGTH_LONG).show() }
    }
    val cs = MaterialTheme.colorScheme
    val focus = androidx.compose.ui.platform.LocalFocusManager.current

    // Autosave: bản đã ghi gần nhất; ghi sau 600ms ngừng gõ, khi rời màn hình, và khi app xuống nền (HyperOS hay giết nền)
    var title by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(item.title) }
    var remind by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(item.remindAt) }
    var folder by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(item.folderId) }
    var saved by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(listOf<Any?>(item.text, item.tags, item.imagePath, item.title, item.remindAt, item.folderId)) }
    var gone by remember { mutableStateOf(false) }   // đã xoá note ⇒ không ghi đè lại
    val persist by androidx.compose.runtime.rememberUpdatedState {
        val cur = listOf(text, tags.trim().lowercase().replace(Regex("[#\\s]+"), " ").trim(), img, title.trim(), remind, folder)
        if (!gone && cur != saved) {
            if (img != saved[2]) (saved[2] as String?)?.let { java.io.File(it).delete() }
            if (remind != saved[4]) remind?.let { scheduleRemind(ctx, item.id, it, text) } ?: cancelRemind(ctx, item.id)
            saved = cur
            onSave(item.copy(text = text, tags = cur[1] as String, imagePath = img, title = title.trim(), remindAt = remind, folderId = folder,
                status = if (remind != null && remind != item.remindAt) "open" else item.status))
        }
    }
    LaunchedEffect(text, tags, img, title, remind, folder) { kotlinx.coroutines.delay(600); persist() }
    val owner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(owner) {
        val ob = androidx.lifecycle.LifecycleEventObserver { _, e -> if (e == androidx.lifecycle.Lifecycle.Event.ON_STOP) persist() }
        owner.lifecycle.addObserver(ob)
        onDispose { owner.lifecycle.removeObserver(ob); persist() }
    }
    androidx.activity.compose.BackHandler(onBack = onBack)   // Back hệ thống = quay danh sách (autosave đã ghi), không thoát app
    Scaffold(containerColor = cs.background, topBar = {
        androidx.compose.material3.TopAppBar(
            title = {},
            navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            actions = {
                var menu by remember { mutableStateOf(false) }
                Box {
                    IconButton({ menu = true }) { Icon(Icons.Filled.MoreVert, "Thêm") }
                    DropdownMenu(menu, { menu = false }) {
                        DropdownMenuItem({ Text(if (img == null) "Thêm ảnh" else "Đổi ảnh") }, { menu = false; pickImg.launch("image/*") }, leadingIcon = { Icon(Icons.Outlined.Image, null) })
                        if (img != null) DropdownMenuItem({ Text("Bỏ ảnh") }, { menu = false; img = null }, leadingIcon = { Icon(Icons.Filled.Close, null) })
                        DropdownMenuItem({ Text("Xoá ghi chú", color = Tok.Red) }, { menu = false; gone = true; onDelete() }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = Tok.Red) })
                    }
                }
            },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
        )
    }, bottomBar = {
        // toolbar Markdown dính trên bàn phím (kiểu Apple Notes); luôn hiện — soạn và xem là một
        androidx.compose.material3.Surface(Modifier.fillMaxWidth().imePadding(), color = cs.surface, shadowElevation = 6.dp) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MdBtn(Icons.Outlined.FormatBold, "In đậm") { tf = tf.wrap("**") }
                MdBtn(Icons.Outlined.FormatItalic, "In nghiêng") { tf = tf.wrap("*") }
                MdBtn(Icons.Outlined.Title, "Tiêu đề") { tf = tf.linePrefix("## ") }
                MdBtn(Icons.Outlined.FormatListBulleted, "Danh sách") { tf = tf.linePrefix("- ") }
                MdBtn(Icons.Outlined.CheckBox, "Việc cần làm") { tf = tf.linePrefix("- [ ] ") }
                MdBtn(Icons.Outlined.Code, "Mã") { tf = tf.wrap("`") }
            }
        }
    }) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // gợi ý thư mục = thư mục có sẵn trùng tên một tag của note (chưa xếp thư mục); Founder chạm để xác nhận
            val norm = { s: String -> s.lowercase().replace('-', ' ').trim() }
            val suggested = if (folder != null) null else tags.split(' ').filter { it.isNotBlank() }.firstNotNullOfOrNull { t -> folders.firstOrNull { norm(it.name) == norm(t) } }
            val inFolder = folders.firstOrNull { it.id == folder }
            @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
            androidx.compose.foundation.layout.FlowRow(verticalArrangement = Arrangement.spacedBy(0.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.align(Alignment.CenterVertically).clip(CircleShape).background(cs.surfaceVariant).padding(horizontal = 10.dp, vertical = 5.dp)) {
                    IconLabel(Icons.Outlined.Schedule, "${hourOf(item.createdAt)} · ${dayLabel(dayOf(item.createdAt))}", cs.onSurfaceVariant, 12, FontWeight.Medium)
                }
                remind?.let {
                    var rm by remember { mutableStateOf(false) }
                    Box {
                        Box(Modifier.minimumInteractiveComponentSize().clip(CircleShape).background(Tok.Blue.copy(alpha = 0.12f)).clickable(onClickLabel = "Đổi hoặc huỷ nhắc", role = androidx.compose.ui.semantics.Role.Button) { rm = true }.padding(horizontal = 10.dp, vertical = 5.dp)) {
                            IconLabel(Icons.Outlined.Alarm, remindLabel(it), blueInk(), 12, FontWeight.SemiBold)
                        }
                        DropdownMenu(rm, { rm = false }) {
                            DropdownMenuItem({ Text("Đổi giờ nhắc") }, { rm = false; pickRemind(ctx, it) { at -> remind = at } }, leadingIcon = { Icon(Icons.Outlined.Schedule, null) })
                            DropdownMenuItem({ Text("Huỷ nhắc") }, { rm = false; remind = null }, leadingIcon = { Icon(Icons.Outlined.AlarmOff, null) })
                        }
                    }
                }
                if (remind == null) IconLabelChip("Đặt nhắc", Icons.Outlined.AddAlarm, false) { pickRemind(ctx, System.currentTimeMillis()) { at -> remind = at } }
                inFolder?.let { IconLabelChip(it.name, Icons.Filled.Close, true) { folder = null } }
                suggested?.let { s -> IconLabelChip("Gợi ý: ${s.name}", Icons.Outlined.Folder, false) { folder = s.id } }
            }
            androidx.compose.foundation.text.BasicTextField(
                title, { title = it.replace("\n", "") },
                Modifier.fillMaxWidth().semantics { heading() },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = BeVietnam, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold, color = cs.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(cs.onSurface),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { focus.clearFocus() }),
                decorationBox = { inner -> Box { if (title.isEmpty()) Text("Tiêu đề", color = cs.onSurfaceVariant, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold); inner() } },
            )
            img?.let { path ->
                val bmp by produceState<ImageBitmap?>(null, path) {
                    value = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap()
                    }
                }
                val b = bmp
                var zoom by remember { mutableStateOf(false) }
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), CardDefaults.cardColors(containerColor = cs.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                    // ảnh cao tối đa 240dp (cắt giữa), chạm = xem cả ảnh; Thêm/Đổi/Bỏ ảnh nằm trong ⋮
                    if (b != null) Image(b, "Ảnh đính kèm", Modifier.fillMaxWidth().heightIn(max = 240.dp).padding(6.dp).clip(RoundedCornerShape(12.dp)).clickable(onClickLabel = "Phóng to") { zoom = true }, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                    else Box(Modifier.fillMaxWidth().size(160.dp), Alignment.Center) { Text("Không đọc được ảnh", color = cs.onSurfaceVariant, fontSize = 12.sp) }
                }
                if (zoom && b != null) androidx.compose.ui.window.Dialog({ zoom = false }, androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
                    Image(b, "Ảnh đính kèm", Modifier.fillMaxSize().clickable(onClickLabel = "Đóng") { zoom = false }, contentScale = androidx.compose.ui.layout.ContentScale.Fit)
                }
            }
            TagPicker(tags, knownTags) { tags = it }
            androidx.compose.material3.HorizontalDivider(color = cs.outlineVariant)
            var layout by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
            val md = remember(cs.onSurfaceVariant, cs.surfaceVariant) { mdTransform(cs.onSurfaceVariant, cs.surfaceVariant) }
            androidx.compose.foundation.text.BasicTextField(
                tf, { tf = it },
                Modifier.fillMaxWidth().heightIn(min = 320.dp).pointerInput(Unit) {
                    // chạm ô ☐/☑ (Initial pass, chỉ nuốt lúc nhấc ngón) → đảo dấu, không mở bàn phím; kéo cuộn vẫn đi qua
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false, pass = androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                        val hit = layout?.takeIf { down.position.y <= it.size.height }?.getOffsetForPosition(down.position)?.let { tf.toggleTaskAt(it) } ?: return@awaitEachGesture
                        val up = waitForUpOrCancellation(androidx.compose.ui.input.pointer.PointerEventPass.Initial) ?: return@awaitEachGesture
                        up.consume(); tf = hit
                    }
                },
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = BeVietnam, fontSize = 16.sp, lineHeight = 28.sp, color = cs.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(cs.onSurface),
                visualTransformation = md,
                onTextLayout = { layout = it },
                decorationBox = { inner -> Box { if (text.isEmpty()) Text("Viết ghi chú… hỗ trợ Markdown", color = cs.onSurfaceVariant, fontSize = 16.sp); inner() } },
            )
        }
    }
}

/** Chọn ngày rồi giờ bằng hộp thoại hệ thống; giờ đã qua ⇒ báo, không đặt. */
private fun pickRemind(ctx: android.content.Context, from: Long, onPick: (Long) -> Unit) {
    val c = java.util.Calendar.getInstance().apply { timeInMillis = from.coerceAtLeast(System.currentTimeMillis()) }
    android.app.DatePickerDialog(ctx, { _, y, m, d ->
        android.app.TimePickerDialog(ctx, { _, h, min ->
            val at = java.util.Calendar.getInstance().apply { set(y, m, d, h, min, 0); set(java.util.Calendar.MILLISECOND, 0) }.timeInMillis
            if (at > System.currentTimeMillis()) onPick(at) else android.widget.Toast.makeText(ctx, "Chọn giờ trong tương lai", android.widget.Toast.LENGTH_SHORT).show()
        }, c[java.util.Calendar.HOUR_OF_DAY], c[java.util.Calendar.MINUTE], true).show()
    }, c[java.util.Calendar.YEAR], c[java.util.Calendar.MONTH], c[java.util.Calendar.DAY_OF_MONTH]).show()
}

@Composable
private fun MdBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) =
    androidx.compose.material3.IconButton(onClick) { Icon(icon, label, Modifier.size(22.dp)) }   // phẳng, không nền xám như IconButton của app
