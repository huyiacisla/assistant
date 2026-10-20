package com.huyi.recap.store

import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.shared.model.Page
import com.huyi.recap.shared.model.Status

val Item.state: Status get() = Status.of(status)

/** Trang chứa mục: Note · Recap (ảnh/ghi nhận) · Clipboard (clipboard + link). */
fun Item.inPage(p: Page): Boolean = when (p) {
    Page.Clip -> source == "clipboard" || type == ItemType.Link.db
    Page.Note -> type == ItemType.Note.db && source != "clipboard"
    else -> source != "clipboard" && type != ItemType.Link.db && type != ItemType.Note.db
}

/** Tag theo số lần dùng giảm dần. */
fun List<Item>.tagsByUse(): List<String> =
    flatMap { it.tags.split(' ').filter(String::isNotBlank) }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.map { it.key }
