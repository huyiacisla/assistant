# Phân tích công nghệ — init source (Android MVP)

> Phạm vi đã chốt: dùng cá nhân, không thương mại, không backend. Xem [scope_personal_use_20-10-2026.md](scope_personal_use_20-10-2026.md); mục đó ghi đè các phần liên quan đến Play Store, backend, OEM.

Tiền đề: [feasibility_analysis_20-10-2026.md](feasibility_analysis_20-10-2026.md) ⇒ Android làm trước. Nguyên tắc chọn: ponytail (YAGNI → stdlib/platform → dep đã có → tối thiểu). Phiên bản dep: chốt khi init, tra docs chính thức; chưa kiểm chứng ở đây.

Ký hiệu: ● chọn · ◐ cân nhắc/để sau · ○ loại · — không áp dụng.

## 1. Quyết định stack

| Lớp | Chọn | Lý do | Thay thế | Mức |
|---|---|---|---|---|
| Nền tảng | Android native, Kotlin | overlay · MediaProjection · foreground service cần API native | Flutter/RN/KMP vẫn phải viết native module cho overlay | ● |
| minSdk | 26 | `TYPE_APPLICATION_OVERLAY` từ API 26 | 29+ nếu muốn bỏ nhánh cũ | ● |
| targetSdk | mới nhất ổn định | Play bắt buộc; FGS type, MediaProjection consent per-session | — | ● |
| UI sheet/màn hình | Jetpack Compose | prototype là bottom sheet + card, hợp Compose | XML View | ● |
| UI nút nổi | `View` thường trong `WindowManager` | `ComposeView` trong overlay cần tự gắn `LifecycleOwner`/`SavedStateRegistryOwner`; nút tròn kéo thả không cần Compose | ComposeView + owner thủ công | ● |
| Build | Gradle Kotlin DSL + version catalog | chuẩn Android Studio | Groovy | ● |
| Lưu trữ Kho | Room (SQLite) + FTS | local-first, tìm kiếm chữ OCR | Realm, file JSON | ● |
| Cài đặt | DataStore | thay SharedPreferences | SharedPreferences | ◐ |
| Ảnh | Coil | Compose-native, nhẹ | Glide | ◐ |
| OCR | ML Kit Text Recognition v2 (Latin, bundled) | on-device, miễn phí, offline | Tesseract (chậm), cloud Vision (lộ dữ liệu) | ● |
| Chụp màn hình | `MediaProjection` + `VirtualDisplay` + `ImageReader` | không cần Accessibility | AccessibilityService (rủi ro Play) · `MediaStore` observer (chỉ bắt screenshot hệ thống) | ● |
| Giọng nói | `SpeechRecognizer` vi-VN | platform; on-device recognizer từ API 31 | Whisper on-device (nặng) | ● |
| Clipboard | Activity trong suốt, đọc khi có focus | Android 10+ chặn đọc nền | — | ● |
| Gợi ý tag/thư mục | rule + từ khoá trên text OCR | YAGNI | embedding nhỏ (LiteRT) · LLM cloud opt-in · Gemini Nano nếu máy hỗ trợ | ● MVP · ◐ v2 |
| Thời gian dùng app | `UsageStatsManager` | quyền đặc biệt + Play review | — | ○ MVP |
| DI | constructor injection thủ công | < ~10 class | Hilt khi phình | ● |
| Async | Coroutines + Flow | stdlib Kotlin | RxJava | ● |
| Test | JUnit (logic tag/rule) | 1 check tối thiểu cho mỗi logic không tầm thường | Compose UI test, Robolectric sau | ● |

## 2. Thành phần Android bắt buộc

| Thành phần | Vai trò | Khai báo/điểm cần nhớ |
|---|---|---|
| `OverlayService` (foreground) | giữ nút nổi sống | quyền `SYSTEM_ALERT_WINDOW`; FGS type `specialUse` (+ giải trình Play); notification thường trực |
| `CaptureService` (foreground) | chụp màn hình | FGS type `mediaProjection`; gọi `startForeground` sau khi user đồng ý; đăng ký callback `MediaProjection` (Android 14+) |
| `ClipboardActivity` (theme trong suốt) | đọc clipboard khi bấm nút | mở → đọc ngay `onWindowFocusChanged` → đóng |
| `MainActivity` | Kho, Recap, cài đặt, xin quyền | Compose |
| `SpeechRecognizer` | ghi chú thoại | quyền `RECORD_AUDIO` |
| Room DB | Kho | entity: Item (loại, text, tag, nguồn, thời gian), Folder |

## 3. Cấu trúc source đề xuất

| Đường dẫn | Nội dung |
|---|---|
| `app/` | module Android duy nhất (YAGNI multi-module) |
| `app/src/main/java/.../overlay/` | `OverlayService`, bubble view, menu 5 tác vụ |
| `.../capture/` | `MediaProjection`, `ImageReader` → bitmap |
| `.../ocr/` | wrapper ML Kit |
| `.../voice/` | wrapper `SpeechRecognizer` |
| `.../clipboard/` | activity trong suốt |
| `.../data/` | Room: entity · dao · repository |
| `.../organize/` | rule gợi ý tag/thư mục |
| `.../ui/` | Compose: Kho, Recap, bottom sheet, theme |
| `gradle/libs.versions.toml` | version catalog |

## 4. Các bước init source

| Bước | Việc | Check |
|---|---|---|
| 1 | Android Studio → New Project → Empty Activity (Compose), Kotlin DSL, minSdk 26, package chốt trước | [ ] |
| 2 | Chuyển phiên bản sang `libs.versions.toml`, tra phiên bản dep mới nhất ổn định | [ ] |
| 3 | Thêm dep: Room + KSP, ML Kit text-recognition, Coil, DataStore | [ ] |
| 4 | Manifest: `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` + `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `RECORD_AUDIO`, `POST_NOTIFICATIONS` | [ ] |
| 5 | Màn xin quyền overlay → khởi động `OverlayService` → bubble kéo thả (mốc demo 1) | [ ] |
| 6 | Chạm bubble → menu 5 tác vụ (stub) | [ ] |
| 7 | Chụp màn hình → ML Kit OCR → hiện text (mốc spike, kiểm OCR tiếng Việt có dấu) | [ ] |
| 8 | Lưu Item vào Room, màn Kho + tìm kiếm | [ ] |
| 9 | Ghi chú thoại, lưu clipboard bằng tap | [ ] |
| 10 | Test máy thật trên ≥ 3 hãng (Samsung, Xiaomi, Pixel/Oppo) kiểm chạy nền | [ ] |

## 5. Công cụ chất lượng (theo `AGENTS.md`)

| Công cụ | Dùng cho |
|---|---|
| lean-ctx | đọc/tìm/sửa source |
| ast-grep | tìm/refactor theo cấu trúc Kotlin |
| semgrep | gate bảo mật (quyền, intent export, lộ dữ liệu) |
| jev-mcp | phán đoán/phân loại khi cần |
| ponytail | giữ diff tối thiểu |
| ktlint/detekt | chưa thêm; thêm khi có > 1 contributor |

## 6. Rủi ro công nghệ

| Rủi ro | Mức | Cách kiểm |
|---|---|---|
| `ComposeView` trong overlay lỗi lifecycle | trung | tránh: bubble dùng `View` thường |
| MediaProjection consent mỗi phiên làm phiền | trung | thử UX giữ phiên trong 1 foreground service |
| OCR tiếng Việt có dấu sai | trung | bước 7 test với ảnh quán cafe, post IG, ảnh nghiêng |
| FGS `specialUse` bị Play từ chối | cao | viết giải trình sớm; theo dõi policy |
| OEM kill service | trung | bước 10 |
| Dữ liệu OCR là dữ liệu nhạy cảm | cao | lưu local, không gửi cloud mặc định; cloud AI opt-in |

## 7. Quyết định UI (đã chốt: Jetpack Compose, 2026-10-20)

| Lớp UI | Chọn | Mức |
|---|---|---|
| Khung | Compose + Material 3 tự tùy biến theme (đen/trắng, bo góc lớn) | ● |
| Điều hướng | Navigation Compose (Kho, Thư mục, Recap, Cài đặt) | ● |
| Bottom sheet | `Activity` trong suốt chứa Compose (không dùng `ComposeView` trong `WindowManager`) | ● |
| Lưới Kho | `LazyVerticalStaggeredGrid` | ● |
| Ảnh | Coil | ● |
| Icon | Material Symbols | ● |
| Font | Be Vietnam Pro nhúng `res/font` | ● |
| Thanh tỷ lệ Recap | `LinearProgressIndicator`/`Canvas` tự vẽ | ● |
| Kính mờ thật | Haze; máy < Android 12 dùng nền bán trong suốt | ◐ thêm khi làm sheet, tra docs/bảo trì trước |
| Kéo thả sắp xếp | `Reorderable` | ◐ khi làm "Sắp xếp kho" |
| Biểu đồ phức tạp | Vico | ◐ chỉ khi cần |
| Compose Multiplatform | để sau, chỉ khi chắc làm iOS; đặt UI trong module chung từ đầu nếu có | ◐ chưa quyết |

## 8. Chưa quyết (cần Founder)

| Câu hỏi | Ảnh hưởng |
|---|---|
| Package/applicationId | không đổi được sau khi lên Play |
| ~~Máy thật có sẵn~~ ĐÃ CHỐT: Redmi Turbo 4 Pro, HyperOS 3 / Android 16 (API 36), ROM CN ⇒ targetSdk 36; chỉ test 1 máy; xem [scope_personal_use_20-10-2026.md](scope_personal_use_20-10-2026.md) mục 5 | — |
| Có cần backend/đồng bộ không | MVP đang local-only |
| AI gợi ý: rule vs cloud LLM | chính sách riêng tư, chi phí |
| Có làm iOS không | quyết có dựng Compose Multiplatform ngay từ init |
