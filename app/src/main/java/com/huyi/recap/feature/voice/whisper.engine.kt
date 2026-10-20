package com.huyi.recap.feature.voice


/** whisper.cpp (offline): model ggml đặt ở getExternalFilesDir/whisper.bin (adb push), 16kHz mono float. */
object Whisper {
    init { System.loadLibrary("recapstt") }
    private var ctx = 0L
    private external fun init(path: String): Long
    private external fun run(ctx: Long, threads: Int, pcm: FloatArray): String

    @Synchronized fun load(modelPath: String) { if (ctx == 0L) ctx = init(modelPath).also { require(it != 0L) { "Không mở được model" } } }

    @Synchronized fun transcribe(modelPath: String, pcm: FloatArray): String {
        load(modelPath)
        val t = System.currentTimeMillis()
        return run(ctx, 4, pcm).trim().also { android.util.Log.d("Whisper", "${pcm.size / 16000f}s audio → ${System.currentTimeMillis() - t}ms") }
    }
}
