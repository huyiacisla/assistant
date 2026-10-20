package com.huyi.recap.store

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room

/** type: "note" | "link" | "image" | "text". folderId null = "Chưa phân loại". tags: slug ngăn bằng dấu cách. */
@Entity
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "note",
    val text: String = "",
    val imagePath: String? = null,
    val audioPath: String? = null,    // file ghi âm (WAV) do nhấn giữ mic; phát lại khi nhắc
    val source: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val folderId: Long? = null,
    val tags: String = "",
    val aiStatus: String = "pending", // pending | done | error
    val remindAt: Long? = null,       // epoch ms, parse từ nội dung ghi chú
    val status: String = "open",      // open | done | ended | snoozed
    val remindCount: Int = 0,         // số lần thông báo nhắc đã nổ
    val kind: String = "",            // loại do AI chọn (xem capture/Ai.kt KINDS); "" = chưa phân loại
    val uid: String = java.util.UUID.randomUUID().toString(), // id ổn định giữa các máy (Drive sync); id Room chỉ là cục bộ
    val updatedAt: Long = System.currentTimeMillis(),         // tự cập nhật bằng trigger item_touch khi UPDATE
    val title: String = "",           // tiêu đề do AI đặt ("" = lấy dòng đầu của text)
    val summary: String = "",         // tóm tắt do AI viết ("" = lấy phần còn lại của text)
    val deletedAt: Long? = null,      // xoá tạm: epoch ms; null = còn dùng; >30 ngày ⇒ xoá vĩnh viễn (TRASH_DAYS)
)

const val TRASH_DAYS = 30L
