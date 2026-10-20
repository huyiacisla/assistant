package com.huyi.recap.feature.remind

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.huyi.recap.app.MainActivity
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM")
fun remindLabel(at: Long): String = FMT.format(java.time.Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()))

private fun pi(c: Context, id: Long, text: String) = PendingIntent.getBroadcast(
    c, id.toInt(), Intent(c, RemindReceiver::class.java).putExtra("id", id).putExtra("text", text),
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

// setAlarmClock: đúng giờ, xuyên Doze, không cần quyền exact-alarm; đổi lại có biểu tượng đồng hồ trên thanh trạng thái
// Bị từ chối (SecurityException trên một số ROM/API) → không văng app: báo toast, note vẫn lưu.
fun scheduleRemind(c: Context, id: Long, at: Long, text: String) {
    runCatching {
        c.getSystemService(AlarmManager::class.java).setAlarmClock(
            AlarmManager.AlarmClockInfo(at, PendingIntent.getActivity(c, 0, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)), pi(c, id, text))
    }.onFailure {
        android.util.Log.w("Recap", "đặt nhắc lỗi: $it")
        android.os.Handler(android.os.Looper.getMainLooper()).post { android.widget.Toast.makeText(c, "Chưa đặt được nhắc giờ — kiểm tra quyền báo thức", android.widget.Toast.LENGTH_LONG).show() }
    }
}

fun cancelRemind(c: Context, id: Long) {
    c.getSystemService(AlarmManager::class.java).cancel(pi(c, id, ""))
    c.getSystemService(NotificationManager::class.java).cancel(id.toInt())
}
