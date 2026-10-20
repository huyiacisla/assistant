package com.huyi.recap.shared.model

/** Item.type; `db` là chuỗi đã lưu trong DB (không đổi). Loại AI (Item.kind) vẫn là KINDS ở feature/ai. */
enum class ItemType(val db: String) { Note("note"), Link("link"), Image("image"), Text("text") }
