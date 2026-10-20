package com.huyi.recap.feature.note

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.core.util.dayLabel
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.feature.ai.KINDS
import com.huyi.recap.feature.remind.cancelRemind
import com.huyi.recap.feature.remind.scheduleRemind
import com.huyi.recap.shared.model.Status
import com.huyi.recap.shared.ui.DayStrip
import com.huyi.recap.store.Item
import com.huyi.recap.store.KhoDao
import com.huyi.recap.store.state
import com.huyi.recap.store.tagsByUse
import com.huyi.recap.ui.IconButton
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(dao: KhoDao, notes: List<Item>, f: NoteFilterState, onOpen: (Long) -> Unit, onDelete: (Item) -> Unit, onTrash: () -> Unit) {
    val scope = rememberCoroutineScope()
    val appCtx = androidx.compose.ui.platform.LocalContext.current
    val nq = f.nq
    val filtering = f.filtering
    val shown0 = f.weekNotes(notes)
    val shown = if (f.statF == null) shown0 else shown0.filter { statGroup(it) == f.statF }
    var menu by remember { mutableStateOf(false) }
    val kinds = notes.map { it.kind }.distinct().sortedBy { KINDS.keys.indexOf(it).let { i -> if (i < 0) 99 else i } }
    val tags = notes.tagsByUse().take(10)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box {
            val n = listOf(nq.isNotEmpty(), f.kindF.isNotEmpty(), f.tagF.isNotEmpty(), f.partF.isNotEmpty()).count { it }
            IconButton({ menu = true }) {
                BadgedBox({ if (n > 0) Badge { Text("$n") } }) { Icon(Icons.Filled.FilterList, "Lọc", tint = if (n > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (menu) androidx.compose.material3.ModalBottomSheet({ menu = false }) {
                Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.TextField(f.noteQ, { f.noteQ = it }, Modifier.fillMaxWidth(), placeholder = { Text("Tìm trong ghi chú") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors(),
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        trailingIcon = { if (f.noteQ.isNotEmpty()) IconButton({ f.noteQ = "" }) { Icon(Icons.Filled.Close, "Xoá") } })
                    FilterCombo("Loại", kinds.map { it to (KINDS[it] ?: "Chưa phân loại") }, f.kindF) { f.kindF = it }
                    if (tags.isNotEmpty()) FilterCombo("Tag", tags.map { it to "#$it" }, f.tagF) { f.tagF = it }
                    FilterCombo("Buổi", Part.entries.map { it.name to it.label }, f.partF.map { it.name }.toSet()) { v -> f.partF = v.map { Part.valueOf(it) }.toSet() }
                    FilterCombo("Nhóm theo", listOf("buoi" to "Buổi", "loai" to "Loại"), setOf(if (f.byKind) "loai" else "buoi"), multi = false) { f.byKind = "loai" in it }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton({ menu = false; onTrash() }) { Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Thùng rác") }
                        if (n > 0) TextButton({ f.noteQ = ""; f.kindF = emptySet(); f.tagF = emptySet(); f.partF = emptySet() }) { Icon(Icons.Outlined.FilterAltOff, "Xoá lọc", tint = Tok.Red) }
                    }
                }
            }
        }
        StatusTabs(f.statF, { k -> shown0.count { statGroup(it) == k } }, Modifier.weight(1f)) { f.statF = it }
    }
    DayStrip(f.day, notes.map { dayOf(it.createdAt) }.toSet()) { f.day = it }
    Text(if (!filtering) dayLabel(f.day) else "${shown.size} kết quả trong tuần", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (shown.isNotEmpty() && !filtering) Text("← Xoá · Xong → · chạm = mở · giữ = menu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (shown.isEmpty()) Text(if (!filtering) "Chưa có ghi chú" else "Không thấy", Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
    var folded by remember { mutableStateOf(setOf<String>()) }
    val tog = { k: String -> folded = if (k in folded) folded - k else folded + k }
    val ls = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(shown.firstOrNull()?.id) { ls.scrollToItem(0) }
    LazyColumn(Modifier.fillMaxSize(), state = ls, contentPadding = PaddingValues(top = 4.dp, bottom = 110.dp)) {
        val setStatus = { it: Item, s: String ->
            scope.launch {
                when (Status.of(s)) {
                    Status.Done, Status.Ended -> { cancelRemind(appCtx, it.id); dao.update(it.copy(status = s)) }
                    Status.Snoozed -> { val at = System.currentTimeMillis() + 3_600_000; dao.update(it.copy(status = s, remindAt = at)); scheduleRemind(appCtx, it.id, at, it.text) }
                    else -> { dao.update(it.copy(status = Status.Open.db)); it.remindAt?.takeIf { t -> t > System.currentTimeMillis() }?.let { t -> scheduleRemind(appCtx, it.id, t, it.text) } }
                }
            }
        }
        if (filtering) itemsIndexed(shown, key = { _, it -> it.id }) { i, it -> TimelineRow(it, i + 1, { onOpen(it.id) }, { onDelete(it) }, modifier = Modifier.animateItem()) { s -> setStatus(it, s) } }
        else if (f.byKind) {
            (KINDS.keys + "").forEach { k ->
                val g = shown.filter { it.kind == k }
                if (g.isNotEmpty()) {
                    item(key = "k$k") { GroupHeader(KINDS[k] ?: "Chưa phân loại", "", g.size, "k$k" !in folded) { tog("k$k") } }
                    if ("k$k" !in folded) itemsIndexed(g, key = { _, it -> it.id }) { i, it -> TimelineRow(it, i + 1, { onOpen(it.id) }, { onDelete(it) }, modifier = Modifier.animateItem()) { s -> setStatus(it, s) } }
                }
            }
        } else {
            val nowPart = partOf(System.currentTimeMillis())
            (listOf(nowPart) + Part.entries.filter { it != nowPart }).forEach { part ->
                val g = shown.filter { partOf(it.createdAt) == part }
                if (g.isNotEmpty()) {
                    val live = part == nowPart && g.any { isLive(it.createdAt) }
                    item(key = "h${part.name}") { PartHeader(part, g.size, part.name !in folded, live) { tog(part.name) } }
                    if (part.name !in folded) itemsIndexed(g, key = { _, it -> it.id }) { i, it -> TimelineRow(it, i + 1, { onOpen(it.id) }, { onDelete(it) }, modifier = Modifier.animateItem()) { s -> setStatus(it, s) } }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun FilterCombo(label: String, opts: List<Pair<String, String>>, sel: Set<String>, multi: Boolean = true, onChange: (Set<String>) -> Unit) {
    var open by remember { mutableStateOf(false) }
    // M3 combobox; multi-select = menu giữ mở + checkbox (M3 không có sẵn multi)
    val cs = MaterialTheme.colorScheme
    androidx.compose.material3.ExposedDropdownMenuBox(open, { open = it }) {
        androidx.compose.material3.TextField(
            opts.filter { it.first in sel }.joinToString(", ") { it.second }.ifEmpty { "Tất cả" }, {}, Modifier.fillMaxWidth().menuAnchor(), readOnly = true, singleLine = true,
            shape = RoundedCornerShape(16.dp), label = { Text(label) },
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(open) },
            colors = fieldColors(),
        )
        ExposedDropdownMenu(open, { open = false }, shape = RoundedCornerShape(18.dp), containerColor = cs.surface, tonalElevation = 0.dp, shadowElevation = 8.dp) {
            opts.forEach { (k, l) ->
                val on = k in sel
                DropdownMenuItem({ Text(l, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal) },
                    { onChange(if (!multi) setOf(k) else if (on) sel - k else sel + k); if (!multi) open = false },
                    Modifier.padding(horizontal = 6.dp, vertical = 1.dp).clip(RoundedCornerShape(12.dp)).background(if (on) cs.secondaryContainer else Color.Transparent),
                    leadingIcon = {
                        if (multi) androidx.compose.material3.Checkbox(on, null, colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = cs.primary, checkmarkColor = cs.onPrimary))
                        else androidx.compose.material3.RadioButton(on, null, colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = cs.primary))
                    })
            }
        }
    }
}

@Composable
private fun fieldColors() = MaterialTheme.colorScheme.let { cs ->
    androidx.compose.material3.TextFieldDefaults.colors(
        focusedContainerColor = cs.surfaceVariant.copy(alpha = .5f), unfocusedContainerColor = cs.surfaceVariant.copy(alpha = .5f),
        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
    )
}
