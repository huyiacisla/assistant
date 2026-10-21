package com.huyi.recap.sync

import android.content.Context
import androidx.work.*
import com.huyi.recap.store.Db
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/** Sao lưu Drive nền: token im lặng nếu đã cấp quyền; cần đồng ý lại (hết hạn Testing 7 ngày) ⇒ bỏ lượt, bật lại bằng tay. */
class BackupWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences("drive", Context.MODE_PRIVATE)
        val expired = { prefs.edit().putBoolean("reauth", true).apply(); Result.failure() }   // Cài đặt hiện "Hết hạn đăng nhập"; thử lại vô ích
        val token = suspendCancellableCoroutine<String?> { c ->
            DriveAuth.authorize(applicationContext, { c.resume(null) }, { c.resume(it) }, { c.resume(null) })
        } ?: return expired()
        return runCatching { DriveSync.backup(token, Db.get(applicationContext).dao().allRows()) }.fold({
            prefs.edit().putLong("last", System.currentTimeMillis()).putBoolean("reauth", false).apply()
            Result.success()
        }, { if (it is DriveAuthExpired) expired() else Result.retry() })
    }

    companion object {
        private val net = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        fun enable(ctx: Context) = WorkManager.getInstance(ctx).run {
            enqueueUniquePeriodicWork("drive_backup", ExistingPeriodicWorkPolicy.UPDATE, PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS).setConstraints(net).build())
            enqueue(OneTimeWorkRequestBuilder<BackupWorker>().setConstraints(net).build())  // sao lưu ngay lần đầu bật
        }
        fun disable(ctx: Context) { WorkManager.getInstance(ctx).cancelUniqueWork("drive_backup") }
    }
}
