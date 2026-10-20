package com.huyi.recap.feature.voice

import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTransducerModelConfig
import java.io.File

/** sherpa-onnx Zipformer-vi 30M int8 (offline transducer): thư mục getExternalFilesDir/zip30 gồm encoder/decoder/joiner (.onnx) + tokens.txt (adb push). Đo Redmi: RTF ~0,03. */
object Zipformer {
    private var rec: OfflineRecognizer? = null

    fun ready(dir: File) = File(dir, "tokens.txt").exists() && listOf("encoder", "decoder", "joiner").all { p -> dir.listFiles()?.any { it.name.startsWith(p) && it.name.endsWith(".onnx") } == true }

    @Synchronized fun load(dir: File) {
        if (rec != null) return
        fun f(p: String) = dir.listFiles()!!.first { it.name.startsWith(p) && it.name.endsWith(".onnx") }.path
        rec = OfflineRecognizer(null, OfflineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = 16000, featureDim = 80),
            modelConfig = OfflineModelConfig(transducer = OfflineTransducerModelConfig(f("encoder"), f("decoder"), f("joiner")),
                tokens = File(dir, "tokens.txt").path, numThreads = 4, modelType = "transducer"),
            decodingMethod = "greedy_search"))
    }

    /** Model xuất CHỮ HOA không dấu câu ⇒ hạ chữ thường; `aiClean` lo viết hoa + dấu câu. */
    @Synchronized fun transcribe(dir: File, pcm: FloatArray): String {
        load(dir)
        val r = rec!!; val s = r.createStream()
        return try { s.acceptWaveform(pcm, 16000); r.decode(s); r.getResult(s).text.trim().lowercase() } finally { s.release() }
    }
}
