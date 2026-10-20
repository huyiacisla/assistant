package com.huyi.recap.shared.model


/** Trang của Kho; thứ tự = chỉ số tab đáy (0..3). */
enum class Page(val title: String) { Note("Note"), Recap("Recap"), Clip("Clipboard"), Setting("Cài đặt") }
