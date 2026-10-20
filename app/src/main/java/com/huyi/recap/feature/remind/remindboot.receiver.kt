package com.huyi.recap.feature.remind

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huyi.recap.store.Db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Mở lại máy: báo thức bị xoá ⇒ đặt lại cho note còn hạn; note lỡ giờ lúc tắt máy (<24h, chưa nổ) nổ ngay (giờ quá khứ ⇒ báo thức bắn tức thì). */
class RemindBootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val done = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { Db.get(c).dao().upcoming(System.currentTimeMillis()).forEach { scheduleRemind(c, it.id, it.remindAt!!, it.text) } }
            finally { done.finish() }
        }
    }
}
