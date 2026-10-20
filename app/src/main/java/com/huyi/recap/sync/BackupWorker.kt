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
        val token = suspendCancellableCoroutine<String?> { c ->
            DriveAuth.authorize(applicationContext, { c.resume(null) }, { c.resume(it) }, { c.resume(null) })
        } ?: return Result.failure()
        return runCatching { DriveSync.backup(token, Db.get(applicationContext).dao().allRows()) }.fold({
            applicationContext.getSharedPreferences("drive", Context.MODE_PRIVATE).edit().putLong("last", System.currentTimeMillis()).apply()
            Result.success()
        }, { Result.retry() })
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
