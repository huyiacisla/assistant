# Phase 13 — Vá lỗi đợt 1 (từ audit code 21-10)

Nguồn: [audit_screen_feature_workflow_matrix](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md) §5 #1. Phân loại: `04-phases`.

## Quyết định
| ID | Lỗi | Sửa | Vì sao |
|---|---|---|---|
| S4 | Bubble lọt ảnh chụp A11y: `act()` ẩn bubble rồi `closeMenu()` hiện lại | `menu.view.kt`: ẩn bubble bằng `Handler.post` (chạy sau `closeMenu`), vẫn chụp sau 250 ms | 1 dòng, không đổi `closeMenu` dùng ở chỗ khác |
| S3 | `chat()` cắt `user.take(500)`, `aiClean` thay transcript dài bằng bản cụt | `chat(..., limit=500)` thêm tham số; `aiClean` cho tới `CLEAN_MAX=1500`, dài hơn thì trả `null` ⇒ giữ chữ gốc | không mất nội dung; các lệnh khác giữ 500 |
| S7 | Xoá vĩnh viễn không xác nhận | `trash.screen.kt`: nút và quẹt trái chỉ đặt `confirm`; `AlertDialog` mới gọi `purge` | hành động không hoàn tác |

## Checklist
- [x] S4 · S3 · S7 sửa code
- [x] Test: `AiCleanTest` (ranh giới `CLEAN_MAX`)
- [x] `./gradlew :app:testDebugUnitTest :app:assembleDebug` xanh (exit 0)
- [ ] Máy thật: chụp bằng nút nổi (A11y) không còn bubble trong ảnh — chờ Founder
- [ ] Máy thật: ghi âm >500 ký tự giữ nguyên đuôi — chờ Founder
- [ ] Máy thật: Thùng rác → Xoá vĩnh viễn hiện hộp xác nhận (nút + quẹt trái) — chờ Founder

## Giới hạn đã biết
- Chưa kiểm trên thiết bị; S4 dựa vào thứ tự message của `Handler` (post chạy sau `closeMenu`).
- `aiClean` >1500 ký tự bỏ qua làm sạch AI (chữ Whisper thô), chấp nhận.
- Chưa build release, chưa tăng `versionCode`.
