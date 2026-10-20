# Phase 2 — Chụp màn hình + OCR

Mục tiêu: chạm "Chụp & Lưu" → ảnh + chữ OCR vào Kho.

| Việc | Check |
|---|---|
| Spike A: MediaProjection (`CaptureService`, FGS `mediaProjection`, consent) | [x] |
| Spike B: AccessibilityService `takeScreenshot` (cần bật "Cho phép cài đặt bị hạn chế") | [x] |
| So sánh A/B trên máy: số lần chạm, ổn định, bubble có lọt vào ảnh không → chốt 1 | [x] |
| Ẩn bubble/menu trong lúc chụp | [x] |
| ML Kit Text Recognition v2 (Latin, bundled) | [x] |
| Test OCR tiếng Việt có dấu trên ≥ 10 ảnh thật (quán cafe, post IG, ảnh nghiêng) | [ ] |
| Lưu ảnh vào bộ nhớ app + Item vào Room (`ai_status=pending`) | [x] |
| Ảnh không có chữ vẫn lưu được | [ ] |
| Thông báo ngắn "Đã lưu" + Hoàn tác | [x] (đã test máy thật 20-10-2026: ảnh biến mất khỏi Kho) |

Xong khi: từ app bất kỳ, chạm nút → menu → Chụp & Lưu → item xuất hiện trong Kho kèm chữ OCR.
Quyết định cần ghi lại: MediaProjection vs Accessibility; độ chính xác OCR đo được.

## Kết quả test máy thật (20-10-2026)

| Mục | Kết quả |
|---|---|
| Spike A chạy end-to-end | ● bubble → menu (lớp phủ mờ) → consent → ảnh + OCR + Item trong Kho |
| Consent Android 14+ mặc định "Share one app" | ◐ chụp ra ảnh app được chọn, không phải màn hiện tại ⇒ dùng `createScreenCaptureIntent(MediaProjectionConfig.createConfigForDefaultDisplay())` ⇒ "Share entire screen"; vẫn bấm "SHARE SCREEN" mỗi lần (2 chạm + 1 consent) |
| Bubble lọt vào ảnh | ○ không (ẩn trước khi chụp); chip hệ thống "Casting screen" nằm trong ảnh |
| OCR trên 2 ảnh màn Kho | ● đúng gần hết; 1 lỗi dấu ("thử" → "thủ") ở 1 lần chụp; chưa đủ 10 ảnh |
| Còn thiếu | ≥ 10 ảnh thật; đo ms OCR |

## So sánh A (MediaProjection) vs B (AccessibilityService)

| Tiêu chí | A: MediaProjection | B: AccessibilityService `takeScreenshot` |
|---|---|---|
| Số chạm | 3 (bubble → Chụp & Lưu → SHARE SCREEN) | 2 (bubble → Chụp & Lưu) |
| Hộp thoại đồng ý mỗi lần | có | không |
| Chip "Casting screen" lọt vào ảnh | có | không |
| Bubble lọt vào ảnh | không | không |
| Thiết lập một lần | không | bật Trợ năng thủ công (trước đó "Cho phép cài đặt bị hạn chế") |
| Sau `adb install -r` | không | phải bật lại nếu hệ thống tắt (đã bật ổn trên lần cài này) |

**Quyết định (20-10-2026): B là đường chính**, A giữ làm dự phòng khi Trợ năng tắt (code: `OverlayService` kiểm `ShotAccessibilityService.instance`). Lý do: ít chạm hơn, ảnh sạch; chấp nhận quyền Trợ năng vì app chỉ dùng cá nhân. Service không đọc cây view (`accessibilityEventTypes` tối thiểu, không `canRetrieveWindowContent`).
Chưa đo: thời gian chụp+OCR (toast `[A11y]/[MP] lưu …ms` đã có để đọc khi test), 10 ảnh OCR.

## Clipboard + Kho 4 trang (20-10-2026)

| Hạng mục | Trạng thái |
|---|---|
| Bắt copy chữ qua Trợ năng (nút "Copy/Sao chép/复制") → `ClipActivity` đọc → Item `source=clipboard`, bỏ trùng, Hoàn tác | ● Zalo |
| Copy ảnh (URI `image/*` → JPEG + OCR) | ◐ code xong, chưa test ảnh thật |
| Threads | ◐ không phát sự kiện bấm → không tự bắt; dự phòng nút "+ Lưu clipboard hiện tại" ở tab Clipboard (đã test máy thật ●) |
| Kho 4 tab: Recap · Clipboard · Link · Ảnh copy (mỗi mục 1 trang) | ● |

Lý do dùng click thay vì listener: Android 10+ chặn app nền đọc/nhận copy (log `Denying clipboard access`).
Đã dọn: bỏ log `DBG`, `accessibilityEventTypes` = `typeViewClicked`.

### Cập nhật: phát hiện copy qua log (20-10-2026)

| Hạng mục | Trạng thái |
|---|---|
| Phát hiện copy ở **mọi app** (Threads ●, Facebook, Zalo): `OverlayService` đăng ký listener clipboard rỗng + đọc `logcat ClipboardService:E` dòng "Denying clipboard access to com.huyi.recap" → mở `ClipActivity` | ● Threads |
| Cần cấp 1 lần qua adb: `adb shell pm grant com.huyi.recap android.permission.READ_LOGS` (giữ qua `install -r`, mất khi gỡ app) | — |
| Đã bỏ đường bắt cú bấm "Sao chép" (thừa, gây chớp 2 lần); Trợ năng giờ chỉ dùng cho chụp màn hình | ● |
| Dự phòng: menu "Lưu clipboard" + nút ở tab Clipboard | ● |

Rủi ro: phụ thuộc chuỗi log của HyperOS/Android 16; ROM đổi chuỗi hoặc chặn log ⇒ chỉ còn nút thủ công. Chỉ chạy khi nút nổi bật.
