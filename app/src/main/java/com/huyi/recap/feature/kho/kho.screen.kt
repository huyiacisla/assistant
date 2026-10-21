package com.huyi.recap.feature.kho

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.R
import com.huyi.recap.core.icon.IconLabel
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.core.theme.blueInk
import com.huyi.recap.core.util.ftsQuery
import com.huyi.recap.feature.ai.aiAnalyze
import com.huyi.recap.feature.clipboard.readClip
import com.huyi.recap.feature.clipboard.saveClip
import com.huyi.recap.feature.note.NoteScreen
import com.huyi.recap.feature.note.rememberNoteFilter
import com.huyi.recap.feature.organize.suggest
import com.huyi.recap.feature.quicknote.QuickNoteActivity
import com.huyi.recap.feature.recap.RecapScreen
import com.huyi.recap.feature.remind.cancelRemind
import com.huyi.recap.feature.remind.remindLabel
import com.huyi.recap.shared.model.Page
import com.huyi.recap.shared.ui.BottomNav
import com.huyi.recap.store.Folder
import com.huyi.recap.store.Item
import com.huyi.recap.store.KhoDao
import com.huyi.recap.store.TRASH_DAYS
import com.huyi.recap.store.inPage
import com.huyi.recap.store.tagsByUse
import com.huyi.recap.ui.ClipTab
import com.huyi.recap.ui.IconButton
import com.huyi.recap.ui.RecapTab
import com.huyi.recap.ui.SettingTab
import com.huyi.recap.ui.ViewScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ALL = -1L
private const val UNSORTED = -2L

@OptIn(ExperimentalCoroutinesApi::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun KhoScreen(dao: KhoDao, onStartBubble: () -> Unit, go: String? = null, goTick: Int = 0) {
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(if (go == "clip") 2 else if (go == "cards") 1 else 0) }   // go: clip → Clipboard · cards → tab Recap (danh sách card) · recap → Thống kê · note → Note
    var filter by remember { mutableStateOf(ALL) }
    val appCtx = androidx.compose.ui.platform.LocalContext.current
    val nf = rememberNoteFilter()   // state lọc trang Note (notefilter.state.kt)
    var openId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }   // sống qua xoay máy / process bị giết
    var viewing by remember { mutableStateOf<Item?>(null) }
    var folderDialog by remember { mutableStateOf<Folder?>(null) } // id=0 ⇒ tạo mới

    val folders by dao.folders().collectAsState(emptyList())
    val all by remember(query, filter) {
        val q = ftsQuery(query)
        when {
            q.isNotEmpty() -> dao.search(q)
            filter == UNSORTED -> dao.unsorted()
            filter >= 0 -> dao.inFolder(filter)
            else -> dao.all()
        }
    }.collectAsState(emptyList())
    val every by dao.all().collectAsState(emptyList())
    val open = every.firstOrNull { it.id == openId }   // luôn là bản mới nhất trong DB (xoá ⇒ tự thoát chi tiết)
    var screen by remember { mutableStateOf(if (go == "recap") "recap" else "") }
    var organize by remember { mutableStateOf(go == "organize") }
    // menu nổi gửi intent khi app đang mở: chỉ đổi tab/màn, giữ nguyên bộ lọc (không dựng lại KhoScreen)
    LaunchedEffect(goTick) { if (goTick > 0) { tab = when (go) { "clip" -> 2; "cards" -> 1; else -> 0 }; screen = if (go == "recap") "recap" else ""; openId = null; viewing = null } }

    androidx.activity.compose.BackHandler(enabled = screen.isNotEmpty()) { screen = "" }   // Back ở Thống kê/Thùng rác về tab, không thoát app (thấy trên Redmi 21/10)

    // Folder bị xoá khi đang lọc ⇒ quay về Tất cả
    LaunchedEffect(folders) { if (filter >= 0 && folders.none { it.id == filter }) filter = ALL }

    // Xoá tạm: vào thùng rác 30 ngày, Hoàn tác = khôi phục (đặt lại nhắc nếu còn hạn)
    val del = { it: Item ->
        scope.launch {
            cancelRemind(appCtx, it.id); dao.softDelete(it.id, System.currentTimeMillis())
            if (snack.showSnackbar("Đã xoá · giữ 30 ngày", "Hoàn tác", duration = androidx.compose.material3.SnackbarDuration.Short) == androidx.compose.material3.SnackbarResult.ActionPerformed) restoreItem(appCtx, dao, it)
        }; Unit
    }
    // Quá 30 ngày ⇒ xoá vĩnh viễn (cả file ảnh)
    LaunchedEffect(Unit) {
        dao.expired(System.currentTimeMillis() - TRASH_DAYS * 86_400_000L).forEach { purge(dao, it) }
    }

    // AI tự phân loại mục không phải note thường (lần lượt, chỉ ghi `kind`); ponytail: lỗi → "error", không thử lại tới khi sửa tay
    val todo = every.firstOrNull { !it.inPage(Page.Note) && it.kind.isEmpty() && it.aiStatus == "pending" && it.text.isNotBlank() }
    LaunchedEffect(todo?.id) {
        val t = todo ?: return@LaunchedEffect
        val a = com.huyi.recap.feature.ai.aiAnalyze(t.text)
        dao.setAi(t.id, a?.kind ?: "", if (a != null) "done" else "error")
    }

    if (screen == "recap") { RecapScreen(every) { screen = "" }; return }
    if (screen == "trash") { TrashScreen(dao) { screen = "" }; return }

    if (organize) {
        val sg = suggest(every.filter { it.folderId == null })
        AlertDialog(
            onDismissRequest = { organize = false },
            title = { Text("Sắp xếp kho") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (sg.isEmpty()) Text("Chưa có nhóm nào chung tag (cần ≥2 mục chưa phân loại cùng tag).", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    sg.forEach { s ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${s.name} · ${s.items.size} mục", Modifier.weight(1f))
                            TextButton({
                                scope.launch {
                                    val fid = dao.folderIdByName(s.name) ?: dao.insertFolder(Folder(name = s.name))
                                    dao.setFolder(s.items.map { it.id }, fid)
                                }
                            }) { Text("Gom") }
                        }
                    }
                }
            },
            confirmButton = { IconButton({ organize = false }) { Icon(Icons.Filled.Close, "Đóng") } },
        )
    }

    viewing?.let { ViewScreen(it) { viewing = null }; return }

    open?.let { item ->
        DetailScreen(
            item, folders, every.tagsByUse(),
            onSave = { scope.launch { dao.saveDetail(it.id, it.text, it.tags, it.imagePath, it.title, it.remindAt, it.folderId, it.status == "open") } },   // autosave: không đóng màn hình
            onDelete = { openId = null; del(item) },
            onBack = { openId = null },
        )
        return
    }

    folderDialog?.let { f ->
        FolderDialog(
            f, onDismiss = { folderDialog = null },
            onSave = { name ->
                scope.launch { if (f.id == 0L) dao.insertFolder(Folder(name = name)) else dao.updateFolder(f.copy(name = name)) }
                folderDialog = null
            },
            onDelete = if (f.id == 0L) null else ({
                scope.launch { dao.detachFolder(f.id); dao.deleteFolder(f) }
                folderDialog = null
            }),
        )
    }

    val bgLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    val bgAtState = remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val bgAt: State<androidx.compose.ui.geometry.Offset> = bgAtState
    Scaffold(snackbarHost = { SnackbarHost(snack) }, bottomBar = { BottomNav(tab, { tab = it }, { appCtx.startActivity(android.content.Intent(appCtx, com.huyi.recap.feature.quicknote.QuickNoteActivity::class.java)) }, bgLayer, bgAt) }) { pad ->
        Column(Modifier.fillMaxSize().onGloballyPositioned { bgAtState.value = it.positionInWindow() }
            .drawWithContent { bgLayer.record { this@drawWithContent.drawContent() }; drawLayer(bgLayer) }.padding(top = pad.calculateTopPadding()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(Page.entries[tab].title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            }
            if (tab == 0) { NoteScreen(dao, every.filter { it.inPage(Page.Note) }, nf, { openId = it }, { del(it) }) { screen = "trash" }; return@Column }
            when (tab) {
                1 -> RecapTab(every.filter { it.inPage(Page.Recap) }, { viewing = it }, { del(it) }) { screen = "recap" }
                2 -> ClipTab(every.filter { it.inPage(Page.Clip) }, { openId = it.id },{ del(it) }) {
                    OutlinedButton({ readClip(appCtx)?.let { saveClip(appCtx, it) } }, Modifier.fillMaxWidth(), shape = CircleShape) { Icon(Icons.Outlined.ContentPaste, null, Modifier.size(18.dp)); Text("  Lưu clipboard hiện tại") }
                }
                else -> { val trash by dao.trash().collectAsState(emptyList()); SettingTab(every, trash.size, { screen = "trash" }, onStartBubble) }
            }
        }
    }
}

/** Banner đầu Kho (prototype §3.4). Chưa có AI gợi ý ⇒ chỉ báo số mục chưa phân loại; chạm = lọc. */
@Composable
private fun Banner(n: Int, onClick: () -> Unit) = Card(
    Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(20.dp),
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
) {
    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), Alignment.Center) { Icon(painterResource(R.drawable.ic_bolt), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary) }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Kho của bạn đang lộn xộn?", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("$n mục chưa phân loại · chạm để xem", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ItemCard(item: Item, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick), RoundedCornerShape(18.dp),
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), CardDefaults.cardElevation(1.dp), BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item.imagePath?.let { path ->
                val bmp by produceState<ImageBitmap?>(null, path) {
                    value = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap()
                    }
                }
                bmp?.let { Image(it, null, Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))) }
            }
            if (item.text.isNotBlank() || item.imagePath == null)
                Text(item.text.ifBlank { "(ảnh)" }, maxLines = 6, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, lineHeight = 18.sp)
            item.remindAt?.let { IconLabel(Icons.Outlined.Alarm, remindLabel(it), blueInk(), 11, FontWeight.Medium) }
            if (item.tags.isNotBlank()) Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item.tags.split(' ').take(3).forEach { t ->
                    Text("#$t", Modifier.clip(CircleShape).background(Tok.Pastel[(t.hashCode() and 0x7fffffff) % Tok.Pastel.size]).padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp, maxLines = 1, color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun FolderDialog(f: Folder, onDismiss: () -> Unit, onSave: (String) -> Unit, onDelete: (() -> Unit)?) {
    var name by remember { mutableStateOf(f.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (f.id == 0L) "Thư mục mới" else "Sửa thư mục") },
        text = { OutlinedTextField(name, { name = it }, singleLine = true) },
        confirmButton = { IconButton({ if (name.isNotBlank()) onSave(name.trim()) }) { Icon(Icons.Filled.Check, "Lưu") } },
        dismissButton = { Row { onDelete?.let { IconButton(it) { Icon(Icons.Outlined.Delete, "Xoá", tint = Tok.Red) } }; IconButton(onDismiss) { Icon(Icons.Filled.Close, "Huỷ") } } },
    )
}
