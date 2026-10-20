package com.huyi.recap.feature.organize

import com.huyi.recap.store.Item

/** Gợi ý thư mục: tag chung của ≥2 mục chưa phân loại. Mỗi mục chỉ vào 1 nhóm (nhóm lớn trước). */
data class Suggest(val name: String, val items: List<Item>)

fun suggest(unsorted: List<Item>): List<Suggest> {
    val byTag = unsorted.flatMap { i -> i.tags.split(' ').filter { it.isNotBlank() }.distinct().map { it to i } }
        .groupBy({ it.first }, { it.second }).filter { it.value.size >= 2 }
    val used = HashSet<Long>()
    return byTag.entries.sortedByDescending { it.value.size }.mapNotNull { (tag, list) ->
        val fresh = list.filter { used.add(it.id) }
        if (fresh.size >= 2) Suggest(tag.replace('-', ' ').replaceFirstChar { it.uppercase() }, fresh) else null
    }
}
