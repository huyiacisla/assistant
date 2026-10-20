package com.huyi.recap.core.util

/** Phát file ghi âm qua luồng báo thức (nghe được dù đang để chế độ phương tiện nhỏ); lỗi file → null. */
fun playAudio(path: String): android.media.MediaPlayer? = runCatching {
    android.media.MediaPlayer().apply {
        setAudioAttributes(android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_ALARM).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH).build())
        setDataSource(path); prepare(); start()
    }
}.getOrNull()
