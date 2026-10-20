package com.huyi.recap.store

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

// unicode61 mặc định bỏ dấu ⇒ gõ "ca phe" vẫn khớp "cà phê"
@Fts4(contentEntity = Item::class, tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity
data class ItemFts(val text: String, val tags: String)
