package com.huyi.recap.feature.note

import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.huyi.recap.core.util.dayOf
import com.huyi.recap.feature.ai.KINDS
import com.huyi.recap.store.Item
import java.time.LocalDate

/** State lọc/nhóm của trang Note; giữ ở host để sống qua đổi tab. */
class NoteFilterState {
    var noteQ by mutableStateOf("")   // tìm riêng của trang Note
    var day by mutableStateOf(LocalDate.now())
    var statF by mutableStateOf<String?>(null)   // lọc trạng thái Note: todo/done/wait
    var byKind by mutableStateOf(false)          // nhóm Note: Buổi / Loại
    var kindF by mutableStateOf(setOf<String>())   // filter loại, chọn nhiều ("" = chưa phân loại)
    var tagF by mutableStateOf(setOf<String>())
    var partF by mutableStateOf(setOf<Part>())

    val nq get() = noteQ.trim().lowercase()
    val filtering get() = nq.isNotEmpty() || kindF.isNotEmpty() || tagF.isNotEmpty() || partF.isNotEmpty()   // đang lọc ⇒ bỏ giới hạn ngày

    /** Ghi chú hiển thị trước lọc trạng thái: không lọc ⇒ đúng ngày `day`; đang lọc ⇒ cả tuần chứa `day`. */
    fun weekNotes(notes: List<Item>): List<Item> {
        val mon = day.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        return if (!filtering) notes.filter { dayOf(it.createdAt) == day } else notes.filter {
            dayOf(it.createdAt).let { d -> !d.isBefore(mon) && d.isBefore(mon.plusDays(7)) } &&
                (nq.isEmpty() || it.text.lowercase().contains(nq) || it.tags.contains(nq) || (KINDS[it.kind] ?: "").lowercase().contains(nq)) &&
                (kindF.isEmpty() || it.kind in kindF) && (tagF.isEmpty() || it.tags.split(' ').any { t -> t in tagF }) && (partF.isEmpty() || partOf(it.createdAt) in partF)
        }
    }
}

@Composable
fun rememberNoteFilter() = remember { NoteFilterState() }
