package com.huyi.recap.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huyi.recap.core.theme.RecapTheme
import com.huyi.recap.feature.kho.KhoScreen
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.store.Db

class MainActivity : ComponentActivity() {
    private var nav by mutableStateOf(0)   // tăng mỗi lần menu nổi gửi intent ⇒ dựng lại KhoScreen với "go" mới (không recreate: HyperOS giữ intent cũ)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) {}
            .launch(android.Manifest.permission.POST_NOTIFICATIONS)
        val dao = Db.get(this).dao()
        setContent { RecapTheme { key(nav) { KhoScreen(dao, ::startBubble, intent.getBooleanExtra("clip", false), intent.getStringExtra("go")) } } }
    }

    override fun onNewIntent(intent: Intent) {   // app đang mở: menu nổi gửi "go" mới → dựng lại để mở đúng tab
        super.onNewIntent(intent); setIntent(intent); nav++
    }

    private fun startBubble() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
            return
        }
        startForegroundService(Intent(this, OverlayService::class.java))
    }
}
