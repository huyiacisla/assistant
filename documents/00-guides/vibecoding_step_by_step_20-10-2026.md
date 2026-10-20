# Vibecoding step-by-step theo kiến trúc & tech stack

Bản "áp vào code" của [vibecoding_workflow](vibecoding_workflow_20-10-2026.md): mỗi bước 1→8 nói rõ **chạm lớp nào, file nào, kiểm bằng gì**. Lệnh build/cài/log → [build_run_device](build_run_device_20-10-2026.md). Stack gốc → [tech_stack_analysis](../_analysis/tech_stack_analysis_20-10-2026.md). Cấu trúc thư mục đề xuất & quy ước tên tệp → [modular_structure](../02-architecture/modular_structure_20-10-2026.md).

## 1. Bản đồ hệ thống (đối chiếu code thật)

| Lớp | Công nghệ | Đường dẫn `app/src/main/java/com/huyi/recap/` | Ghi chú |
|---|---|---|---|
| Nút nổi + menu | `View` thường + `WindowManager` (không Compose) | `overlay/OverlayService.kt` | foreground `specialUse`; icon = `ImageView` + `res/drawable/ic_*.xml` |
| Chụp màn hình | MediaProjection / Accessibility | `capture/CaptureActivity`, `CaptureService`, `Shot`, `ShotAccessibilityService` | FGS `mediaProjection` |
| Sheet nhập (Ghi chú nhanh) | Activity trong suốt + Compose | `capture/VoiceActivity.kt` | dùng chung cho "+" và menu nổi |
| Clipboard | Activity trong suốt | `capture/Clip.kt` | Android 10+ chỉ đọc khi có focus |
| Giọng nói | Whisper offline (C++/CMake, arm64) | `capture/Whisper.kt`, `src/main/cpp/` | `ndk abiFilters = arm64-v8a` |
| AI hiểu câu | DeepSeek (HTTP) | `capture/Ai.kt` | khoá ở `local.properties` → `BuildConfig.DEEPSEEK_API_KEY`; không đọc `.env` |
| Nhắc giờ | alarm + notification | `capture/Remind.kt` | `remindAt` trong Item |
| Dữ liệu | Room (+FTS4), migration thủ công | `data/Db.kt` | `Item` · `Folder` · `ItemFts` · `KhoDao` |
| UI chính | Compose + Material 3 + `material-icons-extended` | `ui/KhoScreen`, `Timeline`, `RecapScreen`, `Organize`, `Theme`, `Ic` | trang theo `inPage()`: 0 Note · 1 Recap · 2 Clipboard · 3 Link · 4 Ảnh |
| Điểm vào | Compose `ComponentActivity` | `MainActivity.kt` | xin quyền, bật nút nổi |

Hai nhánh UI: **Compose+M3** (mọi màn/sheet) vs **View thuần** (chỉ overlay) → việc chạm overlay không dùng Compose/`Icons.*`, dùng drawable XML.

Phiên bản (nguồn: `gradle/libs.versions.toml`): AGP 9.4.1 · Kotlin 2.4.21 · Compose BOM 2026.06.01 · Room 2.8.5 · KSP · ML Kit OCR 16.0.1 · minSdk 26 · target/compile 36.

## 2. Quy trình từng bước

Ký hiệu: ● bắt buộc · ○ tuỳ việc · — bỏ.

| # | Bước | Làm gì (cụ thể cho repo này) | Kiểm | Bắt buộc |
|---|---|---|---|---|
| 1 | Chốt ý | 1–3 câu + tiêu chí đo được; đối chiếu `_analysis/scope_personal_use` (cá nhân, không backend/thương mại) | `jev_noul` "ngoài scope?" | ● |
| 2 | Tra doc | `03-plans/roadmap`, `04-phases/phase_*`, `05-designs/ui_node_checklist`; tìm helper có sẵn bằng `ctx_search` trước khi viết mới | danh sách file liên quan | ● |
| 3 | Thiết kế | Xác định **lớp** (bảng §1) → quyết nhánh: UI Compose / overlay View / data / service. Ladder ponytail: skip → reuse → stdlib → platform → dep có sẵn → 1 dòng | ghi vào phase/`05-designs` | ● |
| 4 | Chia nhỏ | 3–7 bước, mỗi bước build được; thứ tự theo §3 | checklist trong phase file | ● |
| 5 | Code | Từng bước nhỏ qua `ctx_read(anchored)` → `ctx_patch`; style sẵn có; không trừu tượng thừa | build xanh | ● |
| 6 | Build & máy thật | Build → cài → cấp lại `READ_LOGS` → bật nút nổi → thao tác luồng thật → ảnh + log | ảnh/log lưu `05-designs/assets/` | ● nếu chạm UI/quyền/dữ liệu |
| 7 | Kiểm tra | JUnit cho logic (`parseWhen`, `ftsQuery`, tag…), semgrep (quyền, intent export, lộ khoá), ast-grep tìm sót | 0 cảnh báo mới | ● nếu chạm dữ liệu/quyền |
| 8 | Ghi lại | Tick phase + `ui_node_checklist`; ghi quyết định/giới hạn; roadmap; doc mới qua `/doc-classify` | checklist ✓ | ● |

## 3. Quy trình theo loại việc (chọn đúng 1 hàng)

| Việc | Lớp chạm | Thứ tự bước code (5) | Lưu ý riêng |
|---|---|---|---|
| Thêm/đổi màn Compose | `ui/*` | 1) state & dữ liệu từ `KhoDao` → 2) composable → 3) nối điều hướng trong `KhoScreen` → 4) icon `Icons.*` | theme qua `Theme.kt`/`Tok`; kiểm sáng + tối (`cmd uimode night`) |
| Thêm icon | `ui/Ic.kt`, `res/drawable` | Compose: `Icons.Filled/Outlined.*` · View/overlay: `ic_*.xml` 24dp | không dùng emoji/ký tự làm icon |
| Thêm trường dữ liệu | `data/Db.kt` | 1) cột `Item` → 2) **tăng version + migration** → 3) DAO → 4) UI | thiếu migration = mất Kho; kiểm trên bản cài cũ (`install -r`, không gỡ) |
| Sửa nút nổi/menu | `overlay/OverlayService.kt` | `View` + `WindowManager`, không `ComposeView` | `install -r` giết service → bật lại; kiểm `dumpsys … OverlayService` |
| Sửa Ghi chú nhanh | `capture/VoiceActivity.kt` | giữ 1 component cho "+" và nút nổi; lưu qua `save()` | luồng: để trống → không tạo; chip nhắc; ảnh → `filesDir/img_*.jpg` |
| Thêm nguồn chụp/OCR | `capture/*`, ML Kit | permission/FGS type trước → capture → OCR → lưu `Item` | Android 14+: callback MediaProjection |
| AI/giọng nói | `capture/Ai.kt`, `Whisper.kt` | offline trước (Whisper), AI chỉ khi cần; gửi tối thiểu | khoá chỉ ở `local.properties`; không log nội dung nhạy cảm |
| Quyền / Manifest | `AndroidManifest.xml` | khai quyền → màn xin quyền → xử lý từ chối | HyperOS CN: trợ năng/overlay phải bật tay |
| Bug | file lỗi + mọi caller | tái hiện → grep caller → sửa gốc → test | tạo `07-issues/bugs/` nếu lớn |

Quy tắc cấu trúc (chốt 20-10-2026): tệp mới đặt theo [modular_structure](../02-architecture/modular_structure_20-10-2026.md) với tên `<name>.<dạng>.kt`; tệp cũ chuyển khi chạm sửa, mỗi lần 1 tệp.

## 4. Vòng chạy mẫu (1 issue = 1 vòng, liền một mạch)

1. Đọc yêu cầu → hỏi Founder **tối đa 1 lần** (bước 1–4); mơ hồ nhỏ → chọn mặc định ngắn nhất, ghi quyết định.
2. Lập checklist trong phase file → code từng bước, build sau mỗi bước.
3. Máy thật: một phiên adb duy nhất; sau mỗi `install -r` cấp `READ_LOGS`; chụp ảnh ≥ 1 trạng thái sáng.
4. Lỗi thật trên máy → quay bước 5; không suy đoán.
5. Kết: tick checklist, doc, báo cáo gồm đã làm · bằng chứng · giới hạn.

## 5. Cổng "xong"

| Cổng | Đạt khi |
|---|---|
| Build | `assembleDebug` xanh, 0 cảnh báo mới |
| Máy thật | luồng ở tiêu chí bước 1 chạy trên Redmi Turbo 4 Pro; có ảnh/log |
| Dữ liệu | không mất Item cũ sau cài đè; migration kiểm tay |
| Bảo mật | không khoá trong git; quyền khai đủ, không thừa |
| Doc | phase + `ui_node_checklist` tick; quyết định ghi; doc mới đúng thư mục |

## 6. Việc phải hỏi Founder trước

Xoá dữ liệu · gỡ app (mất `READ_LOGS`) · đổi `applicationId` · thêm backend/dep nặng · đổi targetSdk · commit/push.

## 7. Bẫy đã gặp

| Bẫy | Cách tránh |
|---|---|
| `adb install -r` revoke `READ_LOGS` | `adb shell pm grant com.huyi.recap android.permission.READ_LOGS` |
| Nhiều phiên cùng điều khiển máy → tap lệch | một phiên adb tại một thời điểm |
| `adb input text` làm hỏng ký tự có dấu | chỉ là lỗi công cụ test, không phải bug app |
| `Unresolved reference weight` trong `Row` | hàm slot phải là extension `RowScope` |
| Tap vùng cử chỉ hệ thống (mép dưới) → mở recents | tap cách mép ≥ vài chục px |
| `ComposeView` trong overlay lỗi lifecycle | overlay dùng `View` thuần |
