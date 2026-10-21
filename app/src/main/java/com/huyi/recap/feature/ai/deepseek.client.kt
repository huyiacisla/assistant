package com.huyi.recap.feature.ai

import com.huyi.recap.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

internal suspend fun chat(sys: String, user: String, maxTokens: Int, json: Boolean, limit: Int = 500): String? {
    val key = BuildConfig.DEEPSEEK_API_KEY
    if (key.isBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().put("model", "deepseek-chat").put("temperature", 0).put("max_tokens", maxTokens)
                .apply { if (json) put("response_format", JSONObject().put("type", "json_object")) }
                .put("messages", JSONArray().put(JSONObject().put("role", "system").put("content", sys))
                    .put(JSONObject().put("role", "user").put("content", user.take(limit)))).toString()
            val c = URL("https://api.deepseek.com/chat/completions").openConnection() as HttpURLConnection
            c.connectTimeout = 8000; c.readTimeout = 15000; c.doOutput = true
            c.setRequestProperty("Authorization", "Bearer $key"); c.setRequestProperty("Content-Type", "application/json")
            c.outputStream.use { it.write(body.toByteArray()) }
            val reply = c.inputStream.bufferedReader().use { it.readText() }
            JSONObject(reply).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
        }.getOrNull()
    }
}