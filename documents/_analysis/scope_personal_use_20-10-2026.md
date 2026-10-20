# Phạm vi: dùng cá nhân, không thương mại

Ngày: 2026-10-20. Founder chốt: **Android, một người dùng (chính Founder), không phát hành, không thương mại**. Quyết định này **ghi đè** các mục "bản phát hành đại trà" trong [tech_stack_analysis_20-10-2026.md](tech_stack_analysis_20-10-2026.md) và [feature_analysis_ai_20-10-2026.md](feature_analysis_ai_20-10-2026.md).

## 1. Thay đổi so với kế hoạch đại trà

| Hạng mục | Trước | Nay |
|---|---|---|
| Backend proxy (Ktor/Worker), host | bắt buộc | ○ bỏ; app gọi thẳng Jev + DeepSeek bằng OkHttp |
| Lưu 2 key | chỉ ở backend | `local.properties` (ngoài git) → `BuildConfig`; APK chỉ nằm trên máy Founder |
| Module `server/`, `shared/` | đề xuất | ○ bỏ; chỉ module `app/` |
| Play Store, Data Safety, Play Integrity | cần | ○ bỏ; cài APK trực tiếp |
| Opt-in, lọc PII, chính sách riêng tư | cần | ○ bỏ; dữ liệu của chính Founder |
| OEM kill trên nhiều hãng | cần | chỉ chỉnh cho 1 máy của Founder |
| Chụp màn hình | MediaProjection (hỏi quyền mỗi phiên) | ◐ có thể dùng AccessibilityService để chụp 1 chạm; thử cả hai ở spike |
| Thời gian dùng app (Recap) | không đưa vào MVP | ◐ `UsageStatsManager` được phép; chỉ có thời gian foreground theo app |
| Giới hạn tần suất | backend | hạn mức chi tiêu ở dashboard Jev/DeepSeek |

## 2. Giữ nguyên

| Hạng mục | Lý do |
|---|---|
| Kotlin native + Compose + Material 3 | API hệ thống cần native |
| Room/SQLite + FTS, ảnh lưu file | local-first |
| ML Kit OCR on-device | nhanh, offline |
| Phân vai Jev = quyết định, DeepSeek = tổng hợp | theo Founder |
| Lưu Item vào Room trước, gọi AI sau (hàng đợi nhẹ) | app dùng được khi mất mạng |
| Đổi cấu trúc kho cần xác nhận | tránh AI tự sắp xếp sai |
| Clipboard: bấm nút → đọc (Android chặn đọc nền) | giới hạn của OS |
| Ngưỡng xác suất là giả định, đo trên dữ liệu thật | chưa kiểm chứng |

## 3. Lưu ý an toàn khi giữ key trong APK

| Việc | Cách |
|---|---|
| Không đưa APK cho người khác | key trích ra được từ APK |
| Không commit key | `.env`/`local.properties` trong `.gitignore`; Semgrep quét chuỗi khóa |
| Giới hạn thiệt hại nếu lộ | đặt hạn mức chi tiêu; đổi key khi nghi ngờ |
| Android 13+ | bật "Cho phép cài đặt bị hạn chế" cho app cài ngoài Store trước khi cấp Accessibility |

## 4. Kế hoạch rút gọn

| Bước | Việc | Check |
|---|---|---|
| 1 | Scaffold `app/` (Compose, minSdk 26), applicationId tự đặt (không cần giữ lâu dài) | [x] `com.huyi.recap`, build debug OK |
| 2 | Bubble nổi kéo thả + menu 5 tác vụ | [x] test máy thật OK: kéo thả, hít mép, menu 4 mục (3 mục còn là stub), đóng khi chạm ngoài |
| 3 | Chụp màn hình + OCR tiếng Việt, test trên máy thật | [ ] |
| 4 | Room + màn Kho + tìm kiếm | [ ] |
| 5 | Gọi Jev phân loại thư mục (OkHttp), đo độ chính xác trên dữ liệu thật | [ ] |
| 6 | DeepSeek: Recap, tóm tắt | [ ] |
| 7 | Sắp xếp kho (Jev gợi ý gộp + DeepSeek đặt tên, có xác nhận) | [ ] |

## 5. Máy thật (từ DevCheck)

| Mục | Giá trị |
|---|---|
| Máy | Xiaomi Redmi Turbo 4 Pro (`onyx`), Snapdragon 8s Gen 4, RAM 11,78 GB, 264 GB |
| Hệ điều hành | HyperOS 3.0.305.0, Android 16, API 36, aarch64 |
| Bản ROM | `WOLCNXM` ⇒ ROM Trung Quốc (CN) |
| Màn hình | 2772×1280 |

| Hệ quả | Xử lý |
|---|---|
| targetSdk = 36 | edge-to-edge bắt buộc; FGS type khai báo đủ |
| HyperOS giết nền mạnh | bật Tự khởi chạy · Pin "Không hạn chế" · khoá app ở màn hình đa nhiệm |
| Mở Activity từ bubble bị chặn | cấp "Hiển thị cửa sổ bật lên khi chạy nền" (quyền riêng của Xiaomi) |
| Cài APK qua adb | bật USB debugging + "Cài đặt qua USB" (Tuỳ chọn nhà phát triển) |
| Sideload + Accessibility | Android 13+ cần "Cho phép cài đặt bị hạn chế" |
| ROM CN có thể thiếu Google Play Services | ML Kit dùng bản bundled; `SpeechRecognizer` vi-VN có thể không chạy ⇒ spike sớm, dự phòng gọi STT khác |

## 5b. Phiên bản build đã chốt (kiểm chứng bằng build thật, 20-10-2026)

| Mục | Giá trị | Ghi chú |
|---|---|---|
| AGP | 9.4.1 | Kotlin tích hợp sẵn, không dùng plugin `kotlin-android` |
| Gradle | 9.6.0 | AGP 9.4.1 yêu cầu ≥ 9.6.0 |
| Kotlin / Compose compiler | 2.4.21 | plugin `kotlin.plugin.compose` |
| Compose BOM | 2026.06.01 | BOM 2026.09.00 đòi compileSdk 37, SDK máy chỉ có đến 36 |
| compile/target SDK | 36 · minSdk 26 | |
| Chưa thêm | Room, KSP, ML Kit, Coil | thêm khi tới bước tương ứng |

Build: `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`. Cài: `adb install -r` (bật USB debugging + "Cài đặt qua USB" trên HyperOS).

## 6. Chưa quyết

| Câu hỏi | Ảnh hưởng |
|---|---|
| ~~Máy có Google app / Play Services không~~ ĐÃ CHỐT: có Google Play ⇒ `SpeechRecognizer` vi-VN khả dụng; vẫn spike sớm để xác nhận |
| ~~MediaProjection hay AccessibilityService để chụp~~ ĐÃ CHỐT: AccessibilityService (2 chạm, không consent); MediaProjection làm dự phòng — xem phase 2 |
| Có cần Recap theo thời gian dùng app không | thêm quyền Usage Access |
