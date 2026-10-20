package com.huyi.recap.shared.model

/** Item.status; `db` là chuỗi đã lưu trong DB (không đổi). Giá trị lạ ⇒ Open. */
enum class Status(val db: String) {
    Open("open"), Done("done"), Ended("ended"), Snoozed("snoozed");
    companion object { fun of(s: String) = entries.firstOrNull { it.db == s } ?: Open }
}
