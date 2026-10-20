#include <jni.h>
#include <stdlib.h>
#include "whisper.h"

// ponytail: 1 context, gọi tuần tự từ Kotlin (single-thread executor)
JNIEXPORT jlong JNICALL Java_com_huyi_recap_feature_voice_Whisper_init(JNIEnv *env, jobject t, jstring path) {
    const char *p = (*env)->GetStringUTFChars(env, path, NULL);
    struct whisper_context *c = whisper_init_from_file_with_params(p, whisper_context_default_params());
    (*env)->ReleaseStringUTFChars(env, path, p);
    return (jlong) c;
}

JNIEXPORT jstring JNICALL Java_com_huyi_recap_feature_voice_Whisper_run(JNIEnv *env, jobject t, jlong ctx, jint threads, jfloatArray pcm) {
    struct whisper_context *c = (struct whisper_context *) ctx;
    struct whisper_full_params p = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);   // beam 3 + 30s encoder đo được 17s audio → 57s: quá chậm
    p.print_realtime = p.print_progress = p.print_timestamps = p.print_special = false;
    p.language = "vi"; p.translate = false; p.n_threads = threads; p.no_context = true; p.single_segment = false;
    int len = (*env)->GetArrayLength(env, pcm);
    p.no_timestamps = true;
    int ac = len / 320 + 150;   // encoder chỉ xử lý độ dài thật + 3s đệm (giữ ngữ cảnh) thay vì luôn 30s
    p.audio_ctx = ac > 1500 ? 1500 : ac;
    p.max_tokens = len / 1000 + 24;   // ~16 token/s + đệm (10/s từng cắt mất đuôi câu): chặn decoder lặp tới 224 token (đo 3s audio → 22s)
    p.temperature_inc = 0.0f;         // tắt decode lại khi nghi ngờ (mỗi lần lại tốn cả lượt decode)
    jfloat *d = (*env)->GetFloatArrayElements(env, pcm, NULL);
    whisper_reset_timings(c);
    int rc = whisper_full(c, p, d, len);
    (*env)->ReleaseFloatArrayElements(env, pcm, d, JNI_ABORT);
    if (rc != 0) return (*env)->NewStringUTF(env, "");
    char buf[8192]; buf[0] = 0; size_t n = 0;
    for (int i = 0, k = whisper_full_n_segments(c); i < k; i++) {
        const char *s = whisper_full_get_segment_text(c, i);
        for (; *s && n < sizeof(buf) - 1; s++) buf[n++] = *s;
        buf[n] = 0;
    }
    return (*env)->NewStringUTF(env, buf);
}
