package com.huyi.recap.feature.remind

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huyi.recap.R
import com.huyi.recap.shared.model.Status
import com.huyi.recap.store.Db
import com.huyi.recap.store.state
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Tới giờ: thông báo heads-up + popup (full-screen intent khi khoá màn hình; app có quyền overlay nên mở thẳng được khi đang dùng máy). */
class RemindReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val id = i.getLongExtra("id", 0)
        val done = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = Db.get(c).dao()
                val item = dao.byId(id)
                if (item == null || item.deletedAt != null || item.state == Status.Done || item.state == Status.Ended) return@launch
                dao.bumpRemind(id)
                val text = item.text.ifBlank { "Ghi âm nhắc nhở" }
                val nm = c.getSystemService(NotificationManager::class.java)
                nm.createNotificationChannel(NotificationChannel("remind", "Nhắc nhở", NotificationManager.IMPORTANCE_HIGH))
                val popup = PendingIntent.getActivity(c, id.toInt(), Intent(c, RemindActivity::class.java).putExtra("id", id)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                nm.notify(id.toInt(), android.app.Notification.Builder(c, "remind")
                    .setSmallIcon(android.R.drawable.ic_popup_reminder).setContentTitle("Nhắc nhở")
                    .setContentText(text).setStyle(android.app.Notification.BigTextStyle().bigText(text))
                    .setCategory(android.app.Notification.CATEGORY_REMINDER)
                    .setContentIntent(popup).setFullScreenIntent(popup, true).setAutoCancel(true).build())
                runCatching { popup.send() }   // bị chặn (không có overlay) thì vẫn còn thông báo
            } finally { done.finish() }
        }
    }
}
