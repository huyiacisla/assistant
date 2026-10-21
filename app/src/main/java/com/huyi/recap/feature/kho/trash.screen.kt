package com.huyi.recap.feature.kho

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.core.util.hourOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.feature.note.SwipeAct
import com.huyi.recap.feature.note.SwipeCard
import com.huyi.recap.feature.remind.scheduleRemind
import com.huyi.recap.shared.model.Status
import com.huyi.recap.store.Item
import com.huyi.recap.store.KhoDao
import com.huyi.recap.store.TRASH_DAYS
import com.huyi.recap.store.state
import com.huyi.recap.ui.IconButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Khôi phục từ thùng rác; mục còn mở + nhắc chưa tới ⇒ đặt lại báo thức. */
internal suspend fun restoreItem(ctx: android.content.Context, dao: KhoDao, it: Item) {
    dao.restore(it.id)
    val at = it.remindAt
    if (it.state == Status.Open && at != null && at > System.currentTimeMillis()) scheduleRemind(ctx, it.id, at, it.text)
}

/** Xoá vĩnh viễn: dòng DB + file ảnh. */
internal suspend fun purge(dao: KhoDao, it: Item) {
    it.imagePath?.let { p -> withContext(Dispatchers.IO) { java.io.File(p).delete() } }; it.audioPath?.let { p -> withContext(Dispatchers.IO) { java.io.File(p).delete() } }
    dao.delete(it)
}

/** Ô trái 56dp: ảnh nếu có, không thì icon theo loại (ghi âm › link › văn bản › ghi chú). */
@Composable
private fun TrashLead(it: Item) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    val bmp by produceState<ImageBitmap?>(null, it.imagePath) {
        value = it.imagePath?.let { p -> withContext(Dispatchers.IO) { BitmapFactory.decodeFile(p, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() } }
    }
    val b = bmp
    if (b != null) Image(b, null, Modifier.size(56.dp).clip(shape), contentScale = ContentScale.Crop)
    else Box(Modifier.size(56.dp).clip(shape).background(cs.primary.copy(alpha = .12f)), Alignment.Center) {
        val icon = when {
            it.audioPath != null -> Icons.Outlined.Mic
            it.imagePath != null || it.type == "image" -> Icons.Outlined.Image
            it.type == "link" -> Icons.Outlined.Link
            it.type == "text" -> Icons.Outlined.TextSnippet
            else -> Icons.Outlined.StickyNote2
        }
        Icon(icon, null, tint = cs.primary)
    }
}

/** Nút hành động 38dp: nền bo 12dp nhuộm màu hành động (α.12), icon outlined cùng màu — thay vòng tròn xám. */
@Composable
private fun TrashAct(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = .12f)).clickable(onClick = onClick), Alignment.Center) {
        Icon(icon, desc, Modifier.size(20.dp), tint = tint)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun TrashScreen(dao: KhoDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val cs = MaterialTheme.colorScheme
    val list by dao.trash().collectAsState(emptyList())
    var confirm by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Item?>(null) }   // xoá vĩnh viễn phải qua hộp xác nhận
    Scaffold(containerColor = cs.background, topBar = {
        androidx.compose.material3.TopAppBar(
            title = { Text("Thùng rác", fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = cs.background),
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Giữ $TRASH_DAYS ngày, sau đó xoá vĩnh viễn.", fontSize = 13.sp, color = cs.onSurfaceVariant)
            if (list.isEmpty()) Text("Trống", Modifier.padding(top = 8.dp), color = cs.onSurfaceVariant)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(list, key = { it.id }) { it ->
                    val left = (TRASH_DAYS - (System.currentTimeMillis() - (it.deletedAt ?: 0)) / 86_400_000L).coerceAtLeast(0)
                    SwipeCard(RoundedCornerShape(22.dp), Modifier.animateItem(),
                        right = SwipeAct(Icons.Outlined.Restore, "Khôi phục", Color(0xFF2E7D32)) { scope.launch { restoreItem(ctx, dao, it) } },
                        left = SwipeAct(Icons.Outlined.DeleteForever, "Xoá vĩnh viễn", Tok.Red) { confirm = it },
                    ) {
                    Card(Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), CardDefaults.cardColors(containerColor = cs.surface), elevation = CardDefaults.cardElevation(1.dp)) {
                        val title = it.title.ifBlank { it.text.lineSequence().firstOrNull { l -> l.isNotBlank() }?.trim() ?: (if (it.imagePath != null) "Ảnh" else "(trống)") }
                        val rest = it.summary.ifBlank { it.text.lines().dropWhile { l -> l.isBlank() }.drop(1).joinToString(" ").trim() }
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            TrashLead(it)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(title, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                if (rest.isNotEmpty()) Text(rest, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, lineHeight = 18.sp, color = cs.onSurfaceVariant)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${hourOf(it.createdAt)} · ${dayOf(it.createdAt).let { d -> "%02d/%02d/%d".format(d.dayOfMonth, d.monthValue, d.year) }}", fontSize = 12.sp, color = cs.onSurfaceVariant)
                                    Text("· còn $left ngày", fontSize = 12.sp, color = if (left <= 3) Tok.Red else cs.onSurfaceVariant)
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TrashAct(Icons.Outlined.RestoreFromTrash, "Khôi phục", Color(0xFF2E7D32)) { scope.launch { restoreItem(ctx, dao, it) } }
                                TrashAct(Icons.Outlined.DeleteOutline, "Xoá vĩnh viễn", Tok.Red) { confirm = it }
                            }
                        }
                    }
                    }
                }
            }
        }
        confirm?.let { d ->
            androidx.compose.material3.AlertDialog(onDismissRequest = { confirm = null },
                title = { Text("Xoá vĩnh viễn?") }, text = { Text("Không thể khôi phục, kể cả ảnh và ghi âm đính kèm.") },
                confirmButton = { androidx.compose.material3.TextButton({ scope.launch { purge(dao, d) }; confirm = null }) { Text("Xoá", color = Tok.Red) } },
                dismissButton = { androidx.compose.material3.TextButton({ confirm = null }) { Text("Huỷ") } })
        }
    }
}
