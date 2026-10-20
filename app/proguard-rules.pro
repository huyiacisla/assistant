# JNI: tên hàm C gắn với class/package Kotlin (jni.c: Java_com_huyi_recap_feature_voice_Whisper_*) — giữ mọi class có native, kể cả khi package đổi
-keepclasseswithmembers,includedescriptorclasses class * { native <methods>; }

# sherpa-onnx (aar): native gọi ngược vào class/field Java bằng reflection JNI
-keep class com.k2fsa.sherpa.onnx.** { *; }

# ML Kit nạp registrar bằng reflection (newInstance); rule consumer chỉ giữ class, R8 cắt <init>() ⇒ getClient() trả null ⇒ NPE khi Chụp & Lưu (đã gặp ở release 21-10)
-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }

# ML Kit / Play Services tự mang consumer rules; Room, WorkManager, Compose cũng vậy — chỉ thêm khi build release báo thiếu
-dontwarn org.slf4j.**
