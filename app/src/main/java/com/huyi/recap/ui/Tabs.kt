package com.huyi.recap.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.icon.IconLabel
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.core.util.dayLabel
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.core.util.hourOf
import com.huyi.recap.feature.ai.KINDS
import com.huyi.recap.feature.note.GroupHeader
import com.huyi.recap.feature.note.Part
import com.huyi.recap.feature.note.PartHeader
import com.huyi.recap.feature.note.TimelineRow
import com.huyi.recap.feature.note.partOf
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.shared.ui.DayStrip
import com.huyi.recap.store.Db
import com.huyi.recap.store.Item
import com.huyi.recap.sync.BackupWorker
import com.huyi.recap.sync.DriveAuth
import com.huyi.recap.sync.DriveSync
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Chip lọc dạng pill: chọn = nền đen chữ trắng. */
@Composable
private fun FChip(label: String, on: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(label, Modifier.clip(CircleShape).background(if (on) cs.primary else cs.surface).clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (on) cs.onPrimary else cs.onSurface, maxLines = 1)
}

/** Recap: ảnh chụp/ghi nhận màn hình · lịch tuần · AI tự phân loại · nhóm theo buổi, thẻ giống Note. */
@Composable
fun RecapTab(items: List<Item>, onOpen: (Item) -> Unit, onDelete: (Item) -> Unit, onStats: () -> Unit) {
    val ctx = LocalContext.current
    var day by remember { mutableStateOf(LocalDate.now()) }
    var kind by remember { mutableStateOf<String?>(null) }   // null = tất cả, "" = AI chưa phân loại
    val kinds = items.map { it.kind }.filter { it.isNotEmpty() }.distinct()
    val shown = items.filter { dayOf(it.createdAt) == day && (kind == null || it.kind == kind) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onStats) { Icon(Icons.Outlined.BarChart, null, Modifier.size(18.dp)); Text("  Thống kê hôm nay") }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FChip("Tất cả ${items.count { dayOf(it.createdAt) == day }}", kind == null) { kind = null } }
            items(kinds) { k -> FChip(KINDS[k] ?: k, kind == k) { kind = if (kind == k) null else k } }
        }
        DayStrip(day, items.map { dayOf(it.createdAt) }.toSet(), allowFuture = false) { day = it }
        Text(dayLabel(day), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (shown.isEmpty()) Text("Chưa có ghi nhận", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        var folded by remember { mutableStateOf(setOf<String>()) }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 110.dp)) {
            val now = partOf(System.currentTimeMillis())
            (listOf(now) + Part.entries.filter { it != now }).forEach { part ->
                val g = shown.filter { partOf(it.createdAt) == part }
                if (g.isNotEmpty()) {
                    item(key = "h${part.name}") { PartHeader(part, g.size, part.name !in folded) { folded = if (part.name in folded) folded - part.name else folded + part.name } }
                    if (part.name !in folded) itemsIndexed(g, key = { _, it -> it.id }) { i, it -> TimelineRow(it, i + 1, { onOpen(it) }, { onDelete(it) }, noStatus = true, tapOpens = true, modifier = Modifier.animateItem(), onShare = { share(ctx, it) }) {} }
                }
            }
        }
    }
}

private val CLIP_TYPES = listOf(ItemType.Image.db to "Ảnh", ItemType.Link.db to "Link", ItemType.Text.db to "Văn bản")

/** Clipboard: ảnh · link · văn bản chung 1 dòng thời gian; nhóm theo ngày, mỗi thẻ ghi rõ giờ + ngày. */
@Composable
fun ClipTab(items: List<Item>, onOpen: (Item) -> Unit, onDelete: (Item) -> Unit, header: @Composable () -> Unit) {
    val ctx = LocalContext.current
    var type by remember { mutableStateOf<String?>(null) }
    val shown = items.filter { type == null || it.type == type }.sortedByDescending { it.createdAt }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        header()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FChip("Tất cả ${items.size}", type == null) { type = null }
            CLIP_TYPES.forEach { (k, l) -> FChip("$l ${items.count { it.type == k }}", type == k) { type = if (type == k) null else k } }
        }
        if (shown.isEmpty()) Text("Chưa có gì", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 110.dp)) {
            shown.groupBy { dayOf(it.createdAt) }.forEach { (d, g) ->
                item(key = "d$d") { GroupHeader(dayLabel(d), "", g.size) }
                itemsIndexed(g, key = { _, it -> it.id }) { i, it -> TimelineRow(it, i + 1, { onOpen(it) }, { onDelete(it) }, showDate = true, noStatus = true, tapOpens = true, modifier = Modifier.animateItem(), onShare = { share(ctx, it) }) {} }
            }
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, sub: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surface).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, Modifier.size(24.dp), tint = cs.primary)
        Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp); Text(sub, fontSize = 12.sp, color = cs.onSurfaceVariant) }
        Icon(Icons.Filled.ChevronRight, null, tint = cs.onSurfaceVariant)
    }
}

/** Cài đặt: đồng bộ (xuất bản sao — không có máy chủ) · thùng rác · thông báo · nút nổi. */
@Composable
fun SettingTab(all: List<Item>, trashCount: Int, onTrash: () -> Unit, onBubble: () -> Unit) {
    val ctx = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingRow(Icons.Outlined.Sync, "Đồng bộ", "Chưa có máy chủ · xuất ${all.size} mục (JSON) để sao lưu/chia sẻ") {
            ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("application/json").putExtra(Intent.EXTRA_TEXT, exportJson(all)), "Sao lưu Recap"))
        }
        val prefs = remember { ctx.getSharedPreferences("drive", android.content.Context.MODE_PRIVATE) }
        var drive by remember { mutableStateOf(prefs.getString("email", null)) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        val toast = { m: String -> android.widget.Toast.makeText(ctx, m, android.widget.Toast.LENGTH_LONG).show() }
        var last by remember { mutableStateOf(prefs.getLong("last", 0)) }
        var busy by remember { mutableStateOf(false) }
        val onToken = { t: String ->
            scope.launch {
                busy = true
                runCatching {
                    prefs.edit().putString("email", com.huyi.recap.sync.DriveAuth.email(t)).apply(); drive = prefs.getString("email", null)
                    if (prefs.getBoolean("restore", false)) {
                        val (n, img) = com.huyi.recap.sync.DriveSync.restore(ctx, t, com.huyi.recap.store.Db.get(ctx).dao())
                        toast("Đã khôi phục $n mục mới, $img ảnh")
                    } else if (prefs.getBoolean("pending", false)) {
                        val (n, img) = com.huyi.recap.sync.DriveSync.backup(t, com.huyi.recap.store.Db.get(ctx).dao().allRows())
                        last = System.currentTimeMillis(); prefs.edit().putLong("last", last).apply()
                        toast("Đã sao lưu $n mục, $img ảnh mới")
                    }
                }.onFailure { toast(it.message ?: "Lỗi Drive") }
                prefs.edit().putBoolean("pending", false).putBoolean("restore", false).apply(); busy = false
            }; Unit
        }
        val consent = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()) {
            com.huyi.recap.sync.DriveAuth.onActivityResult(ctx, it.data, onToken, toast)
        }
        val fmt = { ms: Long -> java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault()).format(ms) }
        SettingRow(Icons.Outlined.Sync, "Google Drive",
            when { busy -> "Đang sao lưu…"; drive == null -> "Chưa kết nối · bấm để đăng nhập"
                else -> "Đã kết nối · $drive · " + (if (last > 0) "sao lưu ${fmt(last)}" else "bấm để sao lưu") }) {
            if (!busy) { prefs.edit().putBoolean("pending", drive != null).apply(); com.huyi.recap.sync.DriveAuth.authorize(ctx, { consent.launch(it) }, onToken, toast) }
        }
        if (drive != null) SettingRow(Icons.Outlined.Sync, "Khôi phục từ Drive", "Gộp mục chưa có trên máy này từ bản sao lưu") {
            if (!busy) { prefs.edit().putBoolean("restore", true).apply(); com.huyi.recap.sync.DriveAuth.authorize(ctx, { consent.launch(it) }, onToken, toast) }
        }
        if (drive != null) {
            var auto by remember { mutableStateOf(prefs.getBoolean("auto", false)) }
            val setAuto = { v: Boolean ->
                auto = v; prefs.edit().putBoolean("auto", v).apply()
                if (v) com.huyi.recap.sync.BackupWorker.enable(ctx) else com.huyi.recap.sync.BackupWorker.disable(ctx)
                Unit
            }
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MaterialTheme.colorScheme.surface).clickable { setAuto(!auto) }.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Outlined.Sync, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) { Text("Tự động sao lưu", fontWeight = FontWeight.SemiBold, fontSize = 15.sp); Text("Mỗi ngày khi có mạng", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                androidx.compose.material3.Switch(auto, setAuto)
            }
        }
        SettingRow(Icons.Outlined.Delete, "Thùng rác", if (trashCount == 0) "Trống · giữ 30 ngày" else "$trashCount mục · giữ 30 ngày", onTrash)
        SettingRow(Icons.Outlined.Notifications, "Thông báo", "Nhắc nhở, âm báo, quyền thông báo của hệ thống") {
            ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName))
        }
        val cs = MaterialTheme.colorScheme
        var on by remember { mutableStateOf(com.huyi.recap.overlay.OverlayService.running) }
        val toggle = { v: Boolean ->
            if (v) { onBubble(); on = Settings.canDrawOverlays(ctx) }
            else { ctx.stopService(Intent(ctx, com.huyi.recap.overlay.OverlayService::class.java)); on = false }
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surface).clickable { toggle(!on) }.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.TouchApp, null, Modifier.size(24.dp), tint = cs.primary)
            Column(Modifier.weight(1f)) { Text("Nút nổi", fontWeight = FontWeight.SemiBold, fontSize = 15.sp); Text("Nút chụp & lưu nhanh trên mọi app", fontSize = 12.sp, color = cs.onSurfaceVariant) }
            androidx.compose.material3.Switch(on, toggle)
        }
    }
}

// ponytail: cắt ở 200k ký tự (giới hạn Binder ~1MB); cần xuất đầy đủ → ghi file + FileProvider
private fun exportJson(all: List<Item>): String {
    val arr = org.json.JSONArray()
    var size = 0
    for (i in all.sortedByDescending { it.createdAt }) {
        val o = org.json.JSONObject().put("createdAt", i.createdAt).put("type", i.type).put("source", i.source).put("text", i.text).put("tags", i.tags)
        size += o.toString().length; if (size > 200_000) break
        arr.put(o)
    }
    return arr.toString(1)
}

/** Xem chi tiết chỉ-đọc (Recap): ảnh lớn + toàn văn chọn/chép được; chỉ có Chia sẻ. */
@Composable
fun ViewScreen(item: Item, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding().verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") }
            Spacer(Modifier.weight(1f))
            IconButton({ share(ctx, item) }) { Icon(Icons.Outlined.Share, "Chia sẻ") }
        }
        IconLabel(Icons.Outlined.Schedule, "${hourOf(item.createdAt)} · ${dayLabel(dayOf(item.createdAt))}", cs.onSurfaceVariant, 13, FontWeight.Medium)
        if (item.title.isNotBlank()) Text(item.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (item.summary.isNotBlank()) Text(item.summary, fontSize = 14.sp, lineHeight = 21.sp, color = cs.onSurfaceVariant)
        item.imagePath?.let { p ->
            val bmp = remember(p) { android.graphics.BitmapFactory.decodeFile(p, android.graphics.BitmapFactory.Options().apply { inSampleSize = 2 })?.asImageBitmap() }
            bmp?.let { androidx.compose.foundation.Image(it, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)), contentScale = androidx.compose.ui.layout.ContentScale.FillWidth) }
        }
        if (item.text.isNotBlank()) {
            var open by remember(item.id) { mutableStateOf(false) }
            val long = item.text.length > 120 || item.text.count { it == '\n' } >= 3
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(item.text, fontSize = 16.sp, lineHeight = 24.sp, maxLines = if (open || !long) Int.MAX_VALUE else 3, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            if (long) Text(if (open) "Thu gọn" else "Xem thêm", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = cs.primary,
                modifier = Modifier.clickable { open = !open })
        }
        if (item.tags.isNotBlank()) Text(item.tags.split(' ').filter { it.isNotBlank() }.joinToString(" ") { "#$it" }, fontSize = 13.sp, color = cs.onSurfaceVariant)
    }
}

/** Chia sẻ ảnh (nếu có) kèm chữ; ảnh đi qua FileProvider. */
fun share(ctx: android.content.Context, i: Item) {
    val send = Intent(Intent.ACTION_SEND).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    if (i.text.isNotBlank()) send.putExtra(Intent.EXTRA_TEXT, i.text)
    val img = i.imagePath?.let(::File)?.takeIf { it.exists() }
    if (img != null) send.setType("image/*").putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.share", img)) else send.setType("text/plain")
    ctx.startActivity(Intent.createChooser(send, null))
}

private fun tagList(s: String) = s.split(' ').filter { it.isNotBlank() }

/** Dòng tag: chip đã chọn (chạm = bỏ) + "+ Tag" mở bottom sheet chọn tag có sẵn / tạo mới. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun TagPicker(tags: String, known: List<String>, onChange: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var sheet by remember { mutableStateOf(false) }
    val cur = tagList(tags)
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        cur.forEach { t -> IconLabelChip("#$t", Icons.Outlined.Close, true) { onChange((cur - t).joinToString(" ")) } }
        IconLabelChip("Tag", Icons.Outlined.Add, false) { sheet = true }
    }
    if (sheet) {
        var draft by remember { mutableStateOf("") }
        val add = { raw: String ->
            val t = raw.trim().trimStart('#').lowercase().replace(Regex("\\s+"), "-")
            if (t.isNotEmpty() && t !in cur) onChange((cur + t).joinToString(" "))
            draft = ""
        }
        androidx.compose.material3.ModalBottomSheet({ sheet = false }, containerColor = cs.surface) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 28.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Chọn tag", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (known + cur).distinct().forEach { t -> val on = t in cur
                        IconLabelChip("#$t", if (on) Icons.Outlined.Check else null, on) { onChange((if (on) cur - t else cur + t).joinToString(" ")) } }
                }
                if (known.isEmpty() && cur.isEmpty()) Text("Chưa có tag — tạo tag đầu tiên bên dưới", fontSize = 13.sp, color = cs.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedTextField(draft, { draft = it }, Modifier.weight(1f), label = { Text("Tạo tag mới") }, singleLine = true, shape = RoundedCornerShape(16.dp),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { add(draft) }),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done))
                    androidx.compose.material3.Button({ add(draft) }, enabled = draft.isNotBlank(), shape = CircleShape) { Icon(Icons.Outlined.Add, "Thêm") }
                }
                androidx.compose.material3.Button({ sheet = false }, Modifier.fillMaxWidth(), shape = CircleShape) { Icon(Icons.Outlined.Check, "Xong") }
            }
        }
    }
}

@Composable
internal fun IconLabelChip(label: String, icon: ImageVector?, on: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    // chọn rồi = pastel theo tên tag (cùng màu với chip trên thẻ danh sách)
    val pastel = Tok.Pastel[(label.hashCode() and 0x7fffffff) % Tok.Pastel.size]
    val fg = if (on) androidx.compose.ui.graphics.Color.Black else cs.onSurface
    Row(Modifier.minimumInteractiveComponentSize().clip(CircleShape).background(if (on) pastel else cs.surface).then(if (on) Modifier else Modifier.border(1.dp, cs.outline, CircleShape)).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = fg)
        icon?.let { Icon(it, null, Modifier.size(14.dp), tint = fg) }
    }
}

/** Nút icon thống nhất: nền tonal tròn 40dp (vùng chạm 48dp), icon 20dp (thay IconButton trần). */
@Composable
fun IconButton(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    androidx.compose.material3.FilledTonalIconButton(
        onClick, modifier.minimumInteractiveComponentSize().size(40.dp),
        colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .06f), contentColor = MaterialTheme.colorScheme.onSurface),
    ) { androidx.compose.foundation.layout.Box(Modifier.size(20.dp), Alignment.Center) { content() } }
