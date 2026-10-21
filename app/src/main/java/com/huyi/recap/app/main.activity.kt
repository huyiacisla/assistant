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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huyi.recap.core.theme.RecapTheme
import com.huyi.recap.feature.kho.KhoScreen
import com.huyi.recap.overlay.OverlayService
import com.huyi.recap.store.Db

class MainActivity : ComponentActivity() {
    private var nav by mutableStateOf(0)   // tăng mỗi lần menu nổi gửi intent mới (không recreate: HyperOS giữ intent cũ)
    private var go by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33) registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) {}
            .launch(android.Manifest.permission.POST_NOTIFICATIONS)
        val dao = Db.get(this).dao()
        go = intent.getStringExtra("go")
        setContent { RecapTheme { KhoScreen(dao, ::startBubble, go, nav) } }
    }

    override fun onNewIntent(intent: Intent) {   // app đang mở: menu nổi gửi "go" mới → KhoScreen đổi tab/màn, không dựng lại (giữ bộ lọc)
        super.onNewIntent(intent); setIntent(intent); go = intent.getStringExtra("go"); nav++
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
