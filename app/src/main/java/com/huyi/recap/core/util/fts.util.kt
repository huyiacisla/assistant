package com.huyi.recap.core.util

/** Chuỗi người dùng gõ → truy vấn FTS prefix an toàn (bỏ ký tự đặc biệt). */
fun ftsQuery(raw: String): String =
    raw.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotEmpty() }.joinToString(" ") { "$it*" }