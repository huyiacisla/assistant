# QA — Máy thật Redmi Turbo 4 Pro (serial 5d6c1a88), kiểm tra qua adb

Status: partial · Ngày: 20-10-2026 · Phân loại: `08-qa`

## Scope
Đọc trạng thái app (version, quyền, báo thức, logcat). Phần UI (nhãn "còn … giờ", chú thích quẹt, badge "Bây giờ", nav bar B10, janky B5) CHƯA kiểm được: màn hình tắt + khoá (mWakefulness=Dozing, isKeyguardShowing=true, mCurrentFocus=NotificationShade) ⇒ theo quy tắc không gửi input, không mở khoá.

## Test Executions
| ID | Case | Status | Evidence |
|---|---|---|---|
| D1 | Phiên bản | passed | versionName=0.1, versionCode=1, target 36 |
| D2 | RECORD_AUDIO | passed | granted=true (appops: foreground, dùng gần nhất ~1h52m trước) |
| D3 | SYSTEM_ALERT_WINDOW | passed | appops allow |
| D4 | USE_EXACT_ALARM | passed | granted=true; SCHEDULE_EXACT_ALARM: không dùng |
| D5 | POST_NOTIFICATIONS | passed | granted=true |
| D6 | Báo thức note test "Mua dao, trứng và gọi mẹ tối mai" | passed | `RTC_WAKEUP origWhen=2026-10-21 19:00:00 RemindReceiver exactAllowReason=allow-listed`; còn 1 báo thức |
| D7 | Logcat crash | passed | không có AndroidRuntime/FATAL cho Recap |
| D8 | Logcat lỗi nhẹ | note | `AppOps: Operation not started SYSTEM_ALERT_WINDOW` (20:46, lúc cài đè, vô hại); lỗi khác là của HyperOS (SLM, DexOpt) |
| D9 | UI Note (nhãn, chú thích, badge, nav bar B10) | blocked | màn hình khoá |
| D10 | gfxinfo sau mở sheet (B5) | blocked | chỉ 3 frame sau launch bị chặn; janky 1/3, p90 300ms (không có ý nghĩa thống kê) |

## Founder làm tay
- [ ] Mở khoá máy, mở Recap: xem nhãn "còn … giờ", chú thích quẹt, 1 badge "Bây giờ", nav bar khi mở sheet (B10)
- [ ] Mic + Whisper: ghi âm thật, đọc câu, kiểm transcript
- [ ] End-to-end: Facebook → nút nổi → chụp → bong bóng Recap
- [ ] Sau khi mở khoá, báo lại để chạy tiếp gfxinfo B5
