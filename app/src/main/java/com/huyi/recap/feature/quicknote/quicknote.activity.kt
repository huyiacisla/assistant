package com.huyi.recap.feature.quicknote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.lifecycle.lifecycleScope
import com.huyi.recap.core.theme.RecapTheme
import com.huyi.recap.core.util.parseWhen
import com.huyi.recap.feature.ai.Analysis
import com.huyi.recap.feature.ai.Brief
import com.huyi.recap.core.util.playAudio
import com.huyi.recap.feature.voice.Whisper
import com.huyi.recap.feature.voice.Zipformer
import kotlinx.coroutines.launch

/** Ghi chú nhanh (prototype "note"): sheet dưới — nghe tiếng Việt → sửa tay được → "Lưu note" → Item(note, source=voice). */
private const val VOICE_RMS = 600   // RMS PCM16 coi là có tiếng nói

/** Mỗi lần ghi âm = 1 clip độc lập (chữ + PCM của đoạn đó); gõ tay = clip không PCM. `text` = các clip nối dòng. */
internal class PcmClip(val text: String, val pcm: ByteArray)

class QuickNoteActivity : ComponentActivity() {
    internal val clips = mutableStateListOf<PcmClip>()
    internal var text: String
        get() = clips.map { it.text }.filter { it.isNotBlank() }.joinToString("\n")
        set(v) {   // thay toàn bộ chữ nhưng giữ âm thanh (gộp vào 1 clip)
            val all = java.io.ByteArrayOutputStream().also { o -> clips.forEach { o.write(it.pcm) } }.toByteArray()
            clips.clear(); if (v.isNotBlank() || all.isNotEmpty()) clips.add(PcmClip(v, all))
        }
    /** Ghi lại file WAV = nối PCM các clip còn lại; hết PCM → xoá file. */
    internal fun rewriteAudio() {
        player?.release(); player = null; playing = -2
        val all = java.io.ByteArrayOutputStream().also { o -> clips.forEach { o.write(it.pcm) } }.toByteArray()
        if (all.isEmpty()) { audio?.let { java.io.File(it).delete() }; audio = null; return }
        val f = audio?.let { java.io.File(it) } ?: java.io.File(filesDir, "voice_${System.currentTimeMillis()}.wav")
        f.outputStream().use { it.write(wavHeader(all.size)); it.write(all) }
        audio = f.absolutePath
    }
    internal var recap by mutableStateOf<String?>(null)
    internal var editing by mutableStateOf(false)   // ô chữ readonly; bấm icon bút để sửa
    internal var asked by mutableStateOf(false)     // đã bấm icon AI → hiện khung phân tích
    internal var aiAt by mutableStateOf<Long?>(null)
    internal var aiVeto by mutableStateOf(false)      // AI: câu này không cần nhắc
    internal var dismissed by mutableStateOf(false)   // người dùng bấm xoá giờ nhắc
    internal fun remindAt(t: String): Long? = if (dismissed) null else pick ?: if (aiVeto) null else parseWhen(t) ?: aiAt
    internal var analysis by mutableStateOf<Analysis?>(null)
    internal var done by mutableStateOf(0)   // bitmask bước AI đã xong: 1 hiểu · 2 giờ · 4 loại · 8 tiêu đề
    internal var brief by mutableStateOf<Brief?>(null)   // ④ tiêu đề + tóm tắt cho thẻ note
    // Thao tác thêm: dán clipboard · đính ảnh · chọn giờ nhắc nhanh
    internal var img by mutableStateOf<String?>(null)
    internal var pick by mutableStateOf<Long?>(null)
    internal var chosen by mutableStateOf<Long?>(null)   // ngày giờ tuỳ chọn: thời điểm của note (+ nhắc nếu ở tương lai)
    internal var picking by mutableStateOf(false)
    internal fun pickDateTime() { picking = true }

    internal val askImg = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            val f = java.io.File(filesDir, "img_${System.currentTimeMillis()}.jpg")
            contentResolver.openInputStream(uri)!!.use { i -> f.outputStream().use { i.copyTo(it) } }
            img = f.absolutePath
        }.onFailure { status = "Lỗi ảnh: ${it.message}" }
    }

    // Nhấn giữ mic = ghi âm (PCM 16kHz → WAV trong files/); nhả tay → lưu file + chép chữ 1 lần (Whisper, model: files/whisper.bin) để parse giờ/AI. Tới giờ nhắc sẽ phát lại file.
    internal var listening by mutableStateOf(false)
    internal var status by mutableStateOf<String?>(null)
    internal var audio by mutableStateOf<String?>(null)
    internal var kept = false   // đã lưu note ⇒ giữ file; thoát mà chưa lưu ⇒ xoá file mồ côi
    internal var player: android.media.MediaPlayer? = null
    internal var playing by mutableStateOf(-2)   // -1 = nghe lại cả file, i = clip i, -2 = không phát
    internal fun play(path: String, id: Int) {
        player?.release(); player = null
        if (playing == id) { playing = -2; return }   // bấm lại = dừng
        player = playAudio(path)?.also { it.setOnCompletionListener { playing = -2 } }; playing = if (player != null) id else -2
    }
    @Volatile internal var rec: android.media.AudioRecord? = null
    internal val pcm = java.io.ByteArrayOutputStream()
    internal val askMic = registerForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) startRec() else status = "Cần quyền micro" }

    internal val zipDir get() = java.io.File(getExternalFilesDir(null), "zip30")
    /** whisper_q.bin (small q, đa ngữ — "nghe kỹ") nếu có, không thì whisper.bin (PhoWhisper-base). Mỗi tiến trình chỉ nạp 1 model Whisper. */
    internal val model get() = java.io.File(getExternalFilesDir(null), "whisper_q.bin").takeIf { it.exists() } ?: java.io.File(getExternalFilesDir(null), "whisper.bin")
    internal var base = ""
    internal var t0 = 0L
    @Volatile internal var firstV = -1; @Volatile internal var lastV = 0; @Volatile internal var voicedN = 0   // byte đầu/cuối có tiếng + số khung 100ms có tiếng
    @Volatile internal var heard = false   // đoạn đang ghi đã có tiếng nói (RMS > VOICE_RMS)
    internal var seg0 = 0   // vị trí trong pcm của đoạn đang ghi (ghi tiếp = nối đuôi file cũ)

    @android.annotation.SuppressLint("MissingPermission")
    internal fun startRec() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { askMic.launch(Manifest.permission.RECORD_AUDIO); return }
        player?.release(); player = null; playing = -2
        val r = android.media.AudioRecord(android.media.MediaRecorder.AudioSource.MIC, 16000, android.media.AudioFormat.CHANNEL_IN_MONO,
            android.media.AudioFormat.ENCODING_PCM_16BIT, 16000 * 2)
        pcm.reset(); seg0 = 0; heard = false; firstV = -1; lastV = 0; voicedN = 0; rec = r; r.startRecording(); listening = true; t0 = android.os.SystemClock.elapsedRealtime(); status = "Đang nghe…"
        committed.clear(); srFailed = false; openSr()
        Thread {
            val buf = ByteArray(3200)
            var voiced = 0L   // lần cuối nghe thấy tiếng nói (0 = chưa có)
            var floor = 900.0   // nền ồn: trung bình trượt của RMS khi KHÔNG có tiếng → phòng ồn vẫn tự dừng được
            while (rec === r && pcm.size() < 16000 * 2 * 60) {   // cắt ở 60s
                val n = r.read(buf, 0, buf.size)
                if (n <= 0) {   // lỗi/chưa có dữ liệu: không quay vô hạn — lỗi thì dừng, 8s chưa có tiếng thì bỏ
                    val bad = n < 0 || (voiced == 0L && android.os.SystemClock.elapsedRealtime() - t0 > 8000)
                    if (bad) { runOnUiThread { if (rec === r) { cancelRec(); if (n < 0) status = "Mic lỗi — thử lại" } }; break }
                    Thread.sleep(20); continue
                }
                val off = synchronized(pcm) { pcm.size().also { pcm.write(buf, 0, n) } }
                sink?.let { o -> try { o.write(buf, 0, n) } catch (_: Exception) { } }   // cấp PCM cho SpeechRecognizer (EXTRA_AUDIO_SOURCE)
                val now = android.os.SystemClock.elapsedRealtime()
                var sum = 0.0; for (i in 0 until n / 2) { val v = (buf[2 * i].toInt() and 0xFF) or (buf[2 * i + 1].toInt() shl 8); sum += v.toShort() * v.toShort().toDouble() }
                val rms = Math.sqrt(sum / (n / 2)); if (rms < floor * 2.2) floor = (floor * 0.95 + rms * 0.05).coerceAtMost(1500.0)
                if (rms > maxOf(VOICE_RMS.toDouble(), floor * 2.2)) { voiced = now; heard = true; if (firstV < 0) firstV = off; lastV = off + n; voicedN++ }   // ponytail: ngưỡng = max(cố định, 2.2× nền ồn)
                val quiet = if (voiced > 0) now - voiced > 2500 else now - t0 > 8000   // nói rồi im 3s → dừng; 8s chưa nói gì → bỏ
                if (quiet) { runOnUiThread { if (rec === r) { if (voiced > 0) stopRec() else cancelRec() } }; break }
            }
        }.start()
    }

    /** Không nghe thấy gì → bỏ đoạn đang ghi, giữ phần cũ. */
    internal fun cancelRec() {
        val r = rec ?: return
        rec = null; listening = false; r.stop(); r.release(); killSr(); pcm.reset()
        status = if (audio != null) "Đã ghi âm" else "Không nghe thấy gì — chạm mic để thử lại"
    }

    internal fun stopRec() {
        val r = rec ?: return
        rec = null; listening = false; r.stop(); r.release()
        val all = synchronized(pcm) { pcm.toByteArray() }
        if (voicedN < 3 || android.os.SystemClock.elapsedRealtime() - t0 < 600) {   // <300ms tiếng = tiếng động, không phải lời (Whisper sẽ bịa câu)
               // chạm nhầm / chưa nói gì → bỏ đoạn này, giữ các clip cũ
            killSr(); pcm.reset(); status = if (heard) null else "Không nghe thấy gì"; return
        }
        val pad = 16000   // cắt im lặng hai đầu, chừa 0,5s: Whisper nhanh hơn + không bịa câu trong khoảng lặng
        val a = (firstV - pad).coerceAtLeast(0) and 1.inv()
        val seg = all.copyOfRange(a, maxOf(a, (lastV + pad).coerceAtMost(all.size)))
        status = "Đã ghi âm"; transcribing = true; lastSeg = seg
        if (sr != null && !srFailed) {   // chữ do SpeechRecognizer: đóng pipe → nó trả kết quả cuối; 6s không thấy → Whisper
            runCatching { sr?.stopListening() }
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ if (transcribing && sr != null) { killSr(); whisper() } }, 6000)
        } else whisper()
    }

    /** "Nghe kỹ": chép lại clip có tiếng gần nhất bằng Whisper small (đa ngữ, đúng từ Anh, chậm ~0,4× thời lượng) rồi thay chữ clip đó. */
    internal fun carefully() {
        val i = clips.indexOfLast { it.pcm.isNotEmpty() }.takeIf { it >= 0 && !transcribing } ?: return
        val seg = clips[i].pcm; transcribing = true; status = "Đang nghe kỹ…"
        Thread {
            val out = transcribe(seg, true)
            runOnUiThread {
                transcribing = false; status = "Đã ghi âm"
                out.onFailure { status = "Nghe kỹ lỗi: ${it.message}" }
                out.getOrNull()?.trim()?.takeIf { it.isNotEmpty() && i < clips.size }?.let { clips[i] = PcmClip(it, seg) }
            }
        }.start()
    }

    /** Ghi âm xong → chép đoạn vừa ghi (Zipformer; thiếu model → Whisper). */
    internal fun whisper() {
        val seg = lastSeg
        Thread {
            val out = transcribe(seg, false)
            runOnUiThread {
                transcribing = false
                out.onSuccess { if (it.isBlank()) status = "Không nghe rõ" }.onFailure { status = "Đã ghi âm (chưa chép chữ: ${it.message})" }
                out.getOrNull()?.trim()?.takeIf { it.isNotEmpty() }?.let { addClip(it, seg) }   // chỉ tạp âm → bỏ, không tạo clip rỗng
                if (intent.getBooleanExtra("careful", false)) { intent.removeExtra("careful"); carefully() }   // hook debug (SttBench --ez careful true): tự chạy "Nghe kỹ"
            }
        }.start()
    }

    internal fun addClip(t: String, seg: ByteArray) {
        clips.add(PcmClip(t, seg)); rewriteAudio()
    }
    internal fun applyText(t: String) { if (t.isNotBlank()) addClip(t, lastSeg) }

    // ── SpeechRecognizer (dịch vụ Google) đọc PCM từ chính app qua pipe ──
    internal val committed = StringBuilder()   // các câu đã chốt của lần ghi này
    internal var sr: android.speech.SpeechRecognizer? = null
    @Volatile internal var sink: java.io.OutputStream? = null
    internal var srFailed = false
    internal var lastSeg = ByteArray(0)
    internal val GOOGLE = android.content.ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.serviceapi.GoogleRecognitionService")

    internal fun live(partial: String) { text = listOf(base, committed.toString().trim(), partial.trim()).filter { it.isNotEmpty() }.joinToString("\n") }

    internal fun openSr() {
        android.util.Log.i("Voice", "openSr sdk=${android.os.Build.VERSION.SDK_INT}")
        srFailed = true; return   // ponytail: HyperOS CN không có Google RecognitionService (lỗi 10) → chỉ Whisper; xoá cả nhánh SR khi chắc không dùng lại
        ("UNREACHABLE_CODE")
        runCatching {
            if (sr == null) sr = android.speech.SpeechRecognizer.createSpeechRecognizer(this, GOOGLE).apply { setRecognitionListener(listener) }
            val i = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "vi-VN").putExtra(android.speech.RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            sr!!.startListening(i)
        }.onFailure { android.util.Log.w("Voice", "SpeechRecognizer: ${it.message}"); srFailed = true; killSr() }
    }

    internal fun killSr() { runCatching { sink?.close() }; sink = null; runCatching { sr?.destroy() }; sr = null }

    /** Google tự cắt câu khi người nói ngừng ~1–2s: nếu còn đang ghi → chốt câu và mở lượt nghe mới. */
    internal fun next(final: String?) {
        android.util.Log.i("Voice", "SR kết quả: \"$final\" (đang ghi=${rec != null})")
        if (!final.isNullOrBlank()) committed.append(final.trim()).append(' ')
        if (rec != null) { live(""); android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ if (rec != null && !srFailed) openSr() }, 150); return }
        val t = committed.toString().trim(); killSr(); transcribing = false
        if (t.isNotEmpty()) applyText(t) else { transcribing = true; whisper() }   // Google không nghe ra gì → Whisper
    }

    internal val listener = object : android.speech.RecognitionListener {
        override fun onPartialResults(b: Bundle) { if (rec != null) live(b.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()) }
        override fun onResults(b: Bundle) = next(b.getStringArrayList(android.speech.SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull())
        override fun onError(e: Int) {
            if (e == android.speech.SpeechRecognizer.ERROR_NO_MATCH || e == android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT) next(null)
            else { android.util.Log.w("Voice", "SR lỗi $e"); srFailed = true; runCatching { sink?.close() }; sink = null; if (rec == null) { killSr(); whisper() } }
        }
        override fun onReadyForSpeech(p: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(v: Float) {}
        override fun onBufferReceived(b: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(t: Int, p: Bundle?) {}
    }

    internal var transcribing by mutableStateOf(false)
    internal var warned = false
    /** Có nháp chưa lưu → lần đầu chỉ cảnh báo, lần hai mới thoát (Back / ✕ / chạm nền). */
    internal fun leave() {
        if (!warned && (text.isNotBlank() || img != null || audio != null)) { warned = true; android.widget.Toast.makeText(this, "Chưa lưu — bấm lần nữa để bỏ nháp", android.widget.Toast.LENGTH_SHORT).show() } else finish()
    }
    internal fun analyze() { if (text.isNotBlank()) { asked = true; done = 0; val t = text;   // bấm lại = thử lại
         lifecycleScope.launch { understand(t) } } }

    internal fun dropAudio() { clips.removeAll { it.pcm.isNotEmpty() }; rewriteAudio() }   // xoá ghi âm = xoá luôn chữ của các clip có tiếng; chữ gõ tay giữ

    override fun onDestroy() {
        rec?.let { it.stop(); it.release() }; rec = null; killSr(); player?.release(); player = null
        if (!kept) audio?.let { java.io.File(it).delete() }
        super.onDestroy()
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)   // Theme.Translucent không vẽ nền thanh hệ thống ⇒ HyperOS tô đen
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge()   // khớp MainActivity: thanh trạng thái/điều hướng trong suốt, không nháy đen
        Thread { runCatching { if (Zipformer.ready(zipDir)) Zipformer.load(zipDir) else if (model.exists()) Whisper.load(model.path) } }.start()  // nạp model sẵn
        setContent { RecapTheme { androidx.activity.compose.BackHandler { leave() }; Sheet(); if (picking) DateTimeDialog() } }
        // ponytail: không tự nghe khi mở; chạm mic để bắt đầu
        if (intent.getBooleanExtra("rec", false) && applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) startRec()   // chỉ bản debug: test mic không cần chạm
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) intent.getStringExtra("wav")?.let { n ->   // chỉ bản debug: nạp WAV 16kHz mono vào đúng luồng chép chữ
            val b = java.io.File(getExternalFilesDir(null), n).readBytes()
            lastSeg = b.copyOfRange(String(b, Charsets.ISO_8859_1).indexOf("data") + 8, b.size); transcribing = true; status = "Đã ghi âm"; whisper()
        }
    }

}
