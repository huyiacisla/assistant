package com.huyi.recap.sync

import android.app.Activity
import android.content.Context
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Đăng nhập Drive (scope drive.file). Token ngắn hạn: mỗi lần dùng gọi lại authorize (im lặng nếu đã cấp quyền). */
object DriveAuth {
    private val req = AuthorizationRequest.builder().setRequestedScopes(listOf(Scope("https://www.googleapis.com/auth/drive.file"))).build()

    /** Có token ngay → onToken; cần người dùng đồng ý → bật màn hình qua [launch]. */
    fun authorize(ctx: Context, launch: (IntentSenderRequest) -> Unit, onToken: (String) -> Unit, onError: (String) -> Unit) {
        Identity.getAuthorizationClient(ctx).authorize(req)
            .addOnSuccessListener { r -> fromResult(r, launch, onToken, onError) }
            .addOnFailureListener { onError(it.message ?: "Lỗi đăng nhập") }
    }

    fun onActivityResult(ctx: Context, data: android.content.Intent?, onToken: (String) -> Unit, onError: (String) -> Unit) =
        runCatching { Identity.getAuthorizationClient(ctx).getAuthorizationResultFromIntent(data).accessToken }
            .fold({ t -> if (t != null) onToken(t) else onError("Không có token") }, { onError(it.message ?: "Đã huỷ") })

    private fun fromResult(r: AuthorizationResult, launch: (IntentSenderRequest) -> Unit, onToken: (String) -> Unit, onError: (String) -> Unit) {
        val pi = r.pendingIntent
        when {
            r.hasResolution() && pi != null -> launch(IntentSenderRequest.Builder(pi.intentSender).build())
            r.accessToken != null -> onToken(r.accessToken!!)
            else -> onError("Không có token")
        }
    }

    /** Email chủ Drive (kiểm tra token dùng được). */
    suspend fun email(token: String): String = withContext(Dispatchers.IO) {
        val c = URL("https://www.googleapis.com/drive/v3/about?fields=user(emailAddress)").openConnection() as HttpURLConnection
        c.connectTimeout = 15_000; c.readTimeout = 60_000
        c.setRequestProperty("Authorization", "Bearer $token")
        try {
            if (c.responseCode == 401) throw DriveAuthExpired()
            if (c.responseCode != 200) error("Drive ${c.responseCode}")
            JSONObject(c.inputStream.bufferedReader().readText()).getJSONObject("user").getString("emailAddress")
        } finally { c.disconnect() }
    }
}
