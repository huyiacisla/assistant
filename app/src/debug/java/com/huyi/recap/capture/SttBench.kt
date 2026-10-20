package com.huyi.recap.capture

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.huyi.recap.feature.ai.aiClean
import com.huyi.recap.feature.quicknote.QuickNoteActivity
import com.huyi.recap.feature.quicknote.transcribe
import com.huyi.recap.feature.voice.Whisper
import java.io.File

/** adb shell am broadcast -n com.huyi.recap/.capture.SttBench --es wav <tên.wav trong files/> [--es model <tên model>] — WAV 16kHz mono 16-bit */
class SttBench : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        if (i.hasExtra("open")) { c.startActivity(Intent(c, QuickNoteActivity::class.java).putExtra("rec", !i.hasExtra("wav")).putExtra("wav", i.getStringExtra("wav")).putExtra("careful", i.getBooleanExtra("careful", false)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); return }   // mở sheet ghi âm không cần chạm
        val dir = c.getExternalFilesDir(null)!!
        val wav = File(dir, i.getStringExtra("wav") ?: return)
        val model = File(dir, i.getStringExtra("model") ?: "whisper.bin")
        val pr = goAsync()
        Thread {
            runCatching {
                val b = wav.readBytes()
                val o = String(b, Charsets.ISO_8859_1).indexOf("data") + 8   // sau tiêu đề chunk "data"
                val pcm = FloatArray((b.size - o) / 2) { k -> ((b[o + 2 * k].toInt() and 0xFF) or (b[o + 1 + 2 * k].toInt() shl 8)).toShort() / 32768f }
                i.getStringExtra("zip")?.let { z ->   // --es zip <thư mục trong files/ chứa encoder/decoder/joiner/tokens>
                    val d = File(dir, z)
                    fun f(p: String) = d.listFiles()!!.first { it.name.startsWith(p) && it.name.endsWith(".onnx") }.path
                    val t = System.currentTimeMillis()
                    val rec = com.k2fsa.sherpa.onnx.OfflineRecognizer(null, com.k2fsa.sherpa.onnx.OfflineRecognizerConfig(
                        featConfig = com.k2fsa.sherpa.onnx.FeatureConfig(sampleRate = 16000, featureDim = 80),
                        modelConfig = com.k2fsa.sherpa.onnx.OfflineModelConfig(
                            transducer = com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig(f("encoder"), f("decoder"), f("joiner")),
                            tokens = File(d, "tokens.txt").path, numThreads = 4, modelType = "transducer"),
                        decodingMethod = "greedy_search"))
                    val tl = System.currentTimeMillis() - t
                    val t1 = System.currentTimeMillis()
                    val st = rec.createStream(); st.acceptWaveform(pcm, 16000); rec.decode(st)
                    val txt = rec.getResult(st).text
                    val run = System.currentTimeMillis() - t1
                    st.release(); rec.release()
                    Log.d("SttBench", "model=$z audio=${pcm.size / 16000f}s load=${tl}ms run=${run}ms text=$txt")
                    val ai = kotlinx.coroutines.runBlocking { aiClean(txt) }
                    Log.d("SttBench", "aiClean → $ai")
                    return@runCatching
                }
                val t0 = System.currentTimeMillis()
                Whisper.load(model.path)
                val tLoad = System.currentTimeMillis() - t0
                val t1 = System.currentTimeMillis()
                val txt = Whisper.transcribe(model.path, pcm)
                val run = System.currentTimeMillis() - t1
                Log.d("SttBench", "model=${model.name} audio=${pcm.size / 16000f}s load=${tLoad}ms run=${run}ms text=$txt")
                val t2 = System.currentTimeMillis()
                val ai = kotlinx.coroutines.runBlocking { aiClean(txt) }
                Log.d("SttBench", "aiClean=${System.currentTimeMillis() - t2}ms → $ai")
            }.onFailure { Log.e("SttBench", "fail", it) }
            pr.finish()
        }.start()
    }
}
