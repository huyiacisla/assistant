# Build release + keystore (Recap)

- Ngày: 20-10-2026 · Phân loại: `09-runbooks` (quy trình vận hành) · Phạm vi: cài lên máy Founder, không phát hành ([scope_personal_use](../_analysis/scope_personal_use_20-10-2026.md))
- Trạng thái: **đã cài release lên Redmi (21/10 00:19)**; §5 chạy 5/6 ca đạt, còn chuyển giọng (thiếu `whisper.bin` trên máy); phát hiện + sửa 1 lỗi R8 (ML Kit, xem §5)

## 1. Đã có

| Mục | Giá trị |
|---|---|
| Keystore | `~/.recap-keys/recap-release.jks` (PKCS12, quyền 600, alias `recap`, RSA 2048, hạn 10 000 ngày) |
| Mật khẩu | `local.properties` (ngoài git): `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` |
| Dấu vân tay SHA-256 | `65:0C:D5:C2:69:2C:60:CA:E4:AA:E1:89:A6:87:B9:AE:E5:A8:A1:09:C0:25:F6:FB:CD:55:B9:2D:CF:A4:32:5A` |
| Cấu hình | `app/build.gradle.kts`: `signingConfigs.release` đọc `RELEASE_*`; `buildTypes.release` bật `isMinifyEnabled` + `isShrinkResources` + ký release (thiếu `RELEASE_*` ⇒ bỏ ký, vẫn build được debug) |
| Luật R8 | `app/proguard-rules.pro`: giữ mọi class có `native` (JNI `Whisper`), `com.k2fsa.sherpa.onnx.**`, `ComponentRegistrar { <init>(); }` (ML Kit) |
| Kết quả build thử | `app-release.apk` **49 MB** (debug 70 MB), ký v2 đúng chứng chỉ trên, `arm64-v8a`, không `debuggable` |

## 2. Sao lưu keystore (làm ngay)

| Việc | Lý do |
|---|---|
| Chép `recap-release.jks` + 4 dòng `RELEASE_*` vào kho mật khẩu hoặc ổ ngoài | **mất keystore = không cập nhật đè được bản đã cài**, phải gỡ app (mất dữ liệu) |
| Không commit, không gửi qua chat/email | `*.jks`, `local.properties` đã trong `.gitignore` |
| Đổi mật khẩu keystore (nếu cần) | `keytool -storepasswd -keystore ~/.recap-keys/recap-release.jks` rồi sửa `local.properties` |

## 3. Build

```bash
cd /Users/docs/persion/Assistivetouch
./gradlew :app:assembleRelease
```

| Đầu ra | Đường dẫn |
|---|---|
| APK | `app/build/outputs/apk/release/app-release.apk` |
| Bản đồ R8 (giữ lại theo từng versionCode, để đọc stack trace) | `app/build/outputs/mapping/release/mapping.txt` |

Kiểm chữ ký (đổi `<ver>` theo build-tools cài trong SDK):

```bash
$ANDROID_HOME/build-tools/<ver>/apksigner verify -v --print-certs app/build/outputs/apk/release/app-release.apk
```

Dấu vân tay `Signer #1 certificate SHA-256` phải khớp mục 1.

## 4. Cài lên Redmi

| Tình huống | Cách |
|---|---|
| Máy đang cài **bản debug** (chữ ký khác) | `adb install` đè sẽ lỗi `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Phải `adb uninstall com.huyi.recap` ⇒ **mất Room DB + ảnh**. Xuất/sao lưu dữ liệu trước |
| Từ lần sau (đã cài release) | `adb -s 5d6c1a88 install -r app/build/outputs/apk/release/app-release.apk` giữ dữ liệu |
| Sau khi gỡ/cài lại | bật lại Trợ năng Recap (HyperOS hiện hộp thoại "nguy hiểm", tự bấm OK) + quyền overlay/thông báo |
| **`install -r` trên HyperOS cũng reset** | `POST_NOTIFICATIONS`, `RECORD_AUDIO`, appop `SYSTEM_ALERT_WINDOW`, Trợ năng, công tắc "Nút nổi" (21/10). Cấp lại: `pm grant com.huyi.recap android.permission.POST_NOTIFICATIONS` (+`RECORD_AUDIO`), `appops set com.huyi.recap SYSTEM_ALERT_WINDOW allow`, bật Trợ năng + "Nút nổi" trong Cài đặt; `READ_LOGS` Founder tự `pm grant` |
| Hộp "Install via USB" của HyperOS | tự từ chối sau 9 s; tick "Remember my choice" rồi Install, tay Founder bấm |

Tuỳ chọn tránh mất dữ liệu: thêm `applicationIdSuffix = ".debug"` cho `buildTypes.debug` để debug và release cài song song. **Chưa làm** vì đổi applicationId ảnh hưởng việc bật Trợ năng và phiên modular đang chạy.

## 5. Kiểm sau khi build release (chạy 21/10 trên Redmi, bản release không debuggable)

R8 có thể làm hỏng tính năng dùng reflection/JNI; chạy thử từng mục trên máy thật.

| Ca | Kỳ vọng | Kết quả 21/10 |
|---|---|---|
| Mở app, tab Note/Recap/Clipboard/Cài đặt | không crash | ✅ |
| Ghi âm → chuyển giọng (Whisper `recapstt`, Zipformer sherpa) | ra chữ; nếu `UnsatisfiedLinkError`/`NoSuchMethodError` ⇒ thêm `-keep` | ⏳ ghi âm bắt/dừng không crash; **chuyển chữ chưa thử**: máy thiếu `whisper.bin` (148 MB, cần tải lại + `adb push` vào `/sdcard/Android/data/com.huyi.recap/files/`) |
| Chụp & Lưu + OCR ML Kit | lưu được note kèm chữ | ✅ sau sửa: lần đầu **NPE** `TextRecognition.getClient` (R8 cắt `<init>()` của `ComponentRegistrar`, ML Kit nạp bằng reflection). Thêm `-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }` → OCR 434 ms, 125 ký tự |
| Room: tạo/xoá/khôi phục note, Thùng rác | đúng dữ liệu | ✅ |
| Nhắc giờ (AlarmManager/`RemindReceiver`) | báo đúng giờ | ✅ hẹn 00:30 → notification kênh `remind` đúng 00:30, không crash (popup `RemindActivity` bị HyperOS chặn nền, thông báo vẫn còn — đúng thiết kế) |
| Gọi DeepSeek (tóm tắt) | có phản hồi | ✅ tiêu đề/tóm tắt/thẻ từ Chụp & Lưu |

**Bài học R8**: rule consumer của thư viện chỉ `-keep class` (không giữ constructor) ⇒ thư viện nạp bằng reflection `newInstance` sẽ vỡ chỉ ở release; thêm `{ <init>(); }`. Dò bằng `mapping/release/usage.txt` (member bị cắt).

Khi crash: lấy stack trace bằng `adb logcat`, giải mã bằng `retrace` + `mapping.txt` đúng bản build.

## 6. Cảnh báo

| Rủi ro | Xử lý |
|---|---|
| **Key DeepSeek vẫn nằm trong APK** (`BuildConfig`); R8 không giấu được | chỉ dùng cá nhân; **không gửi APK cho người khác** (mục 3 scope). Muốn chia sẻ: bỏ key khỏi build, để người dùng nhập key trong Cài đặt |
| Đổi cấu trúc modular/package làm vỡ JNI | luật `-keepclasseswithmembers ... native` giữ theo `native`, nhưng tên hàm C trong `app/src/main/cpp/jni.c` (`Java_com_huyi_recap_feature_voice_Whisper_*`) phải khớp package mới — sửa cả hai khi dời `Whisper` |
| Đổi package/class `ShotAccessibilityService` | cài đặt Trợ năng trên máy trỏ về tên cũ ⇒ phải bật lại |
| `versionCode` | tăng mỗi lần cài bản mới (hiện `1`) |
| Android 13+ chặn app cài ngoài Store | bật "Cho phép cài đặt bị hạn chế" trước khi cấp Trợ năng |

## 7. Checklist khi phiên modular xong

- [x] `./gradlew :app:assembleRelease` xanh (không lỗi R8 "missing class") — 20/10 23:58, 49 MB, đã ký (máy từ chối `INSTALL_FAILED_UPDATE_INCOMPATIBLE` = chữ ký khác debug; `apksigner`/`keytool` bị allowlist chặn nên chưa so dấu vân tay)
- [ ] Sao lưu keystore (mục 2)
- [x] Gỡ bản debug (Founder: không cần sao lưu) — 21/10
- [x] Cài release (`app-release.apk` 51 MB, 00:19), chạy bảng mục 5: 5/6 đạt; còn chuyển giọng (thiếu `whisper.bin`)
- [ ] Chuyển giọng trên release: tải/đẩy `whisper.bin` rồi thử, thêm `-keep` nếu `UnsatisfiedLinkError`
- [x] Giữ `mapping.txt` của bản đang dùng: `app/build/outputs/mapping/release/` (build 21/10 00:19; đã chép sang `~/.recap-keys/mapping-v1/mapping.txt` để retrace khi build lại ghi đè)
- [x] `versionCode` giữ `1` (chưa có bản cài trước đó cần đè)
