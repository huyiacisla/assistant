package com.huyi.recap.feature.remind

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huyi.recap.core.theme.RecapTheme
import com.huyi.recap.core.util.playAudio
import com.huyi.recap.shared.model.Status
import com.huyi.recap.store.Db
import com.huyi.recap.store.Item
import com.huyi.recap.store.KhoDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Popup nhắc: nền trong suốt + hộp thoại; Xong / Hoãn 1 giờ / Đóng. */
class RemindActivity : ComponentActivity() {
    private var item by mutableStateOf<Item?>(null)
    private var player: android.media.MediaPlayer? = null
    private fun play(p: String) { player?.release(); player = playAudio(p) }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
    private fun load(id: Long) = CoroutineScope(Dispatchers.IO).launch { item = Db.get(this@RemindActivity).dao().byId(id) }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setShowWhenLocked(true); setTurnScreenOn(true)
        load(intent.getLongExtra("id", 0))
        setContent { RecapTheme { item?.let { Popup(it) } } }
    }

    override fun onNewIntent(i: Intent) { super.onNewIntent(i); load(i.getLongExtra("id", 0)) }

    @androidx.compose.runtime.Composable private fun Popup(it: Item) {
        fun act(f: suspend (com.huyi.recap.store.KhoDao) -> Unit) {
            CoroutineScope(Dispatchers.IO).launch { f(Db.get(this@RemindActivity).dao()) }
            getSystemService(NotificationManager::class.java).cancel(it.id.toInt()); finish()
        }
        AlertDialog(onDismissRequest = { finish() },
            title = { Text("Nhắc nhở" + (it.remindAt?.let { a -> " · ${remindLabel(a)}" } ?: "")) },
            text = {
                androidx.compose.foundation.layout.Column {
                    Text(it.text.ifBlank { "Ghi âm nhắc nhở" }, maxLines = 8)
                    it.audioPath?.let { p ->
                        androidx.compose.runtime.LaunchedEffect(it.id) { play(p) }   // tới giờ → tự phát
                        TextButton({ play(p) }) { Text("▶ Nghe lại") }
                    }
                }
            },
            confirmButton = { TextButton({ act { d -> cancelRemind(this, it.id); d.setStatus(it.id, Status.Done.db) } }) { Text("Xong") } },
            dismissButton = {
                TextButton({ act { d -> val at = System.currentTimeMillis() + 3_600_000; d.snooze(it.id, Status.Snoozed.db, at); scheduleRemind(this, it.id, at, it.text) } }) { Text("Hoãn 1 giờ") }
                TextButton({ finish() }) { Text("Đóng") }
            })
    }
}
