# Phase 13 — Sửa 3 lỗi luồng chính từ audit (S4 · S3 · S7)

Nguồn: [audit_screen_feature_workflow_matrix](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md) §5 khuyến nghị #1. Phân loại: `04-phases`.
Mục tiêu: bỏ 3 lỗi đã xác nhận bằng code, rẻ, chạm luồng dùng hằng ngày. Chỉ dựa vào code + build + unit test; **chưa thử trên máy thật**.

## Quyết định
| ID | Lỗi | Cách sửa | Vì sao ngắn nhất |
|---|---|---|---|
| S4 | Bubble lọt ảnh chụp A11y: `act()` ẩn bubble, `closeMenu()` hiện lại ngay trước `shoot()` | `menu.view.kt`: ẩn bubble bằng `Handler.post` (chạy sau `closeMenu`), `shoot()` vẫn sau 250 ms | giữ nguyên `closeMenu`, không thêm cờ |
| S3 | `chat()` cắt `user.take(500)`; `aiClean` thay transcript dài bằng bản cụt | `chat(..., limit = 500)` thêm tham số; `aiClean` gửi tới `CLEAN_MAX = 1500`, dài hơn ⇒ trả `null` (giữ chữ gốc); `max_tokens` 400→1200 | các lệnh khác (brief/analyze/when) vẫn 500 như cũ |
| S7 | Xoá vĩnh viễn (nút + quẹt trái) không xác nhận | `trash.screen.kt`: cả hai chỉ gán `confirm = it`; `AlertDialog` "Xoá vĩnh viễn?" mới gọi `purge` | dùng chung 1 hộp thoại cho 2 đường vào |

## Checklist
- [x] S4 sửa `menu.view.kt`
- [x] S3 sửa `deepseek.client.kt` + `understand.process.kt`
- [x] S7 sửa `trash.screen.kt`
- [x] Test: `AiCleanTest` (cleanable ở mốc 600 / 1500 / 1501)
- [x] `./gradlew :app:testDebugUnitTest :app:assembleDebug` xanh (21/10; `AiCleanTest` 1/1 pass; lần build nền đầu lỗi do phiên fork đang sửa code song song, chạy lại sạch)
- [ ] Máy thật (Founder): Chụp & Lưu qua A11y ⇒ ảnh không có bubble
- [ ] Máy thật (Founder): ghi âm >500 ký tự ⇒ chữ không bị cụt đuôi
- [ ] Máy thật (Founder): Thùng rác ⇒ nút/quẹt xoá hiện hộp xác nhận; Huỷ không xoá

## Giới hạn đã biết
- S4: nếu `closeMenu` chạy muộn hơn 250 ms (hiếm) bubble vẫn có thể lọt; kiểm trên Redmi.
- S3: transcript >1500 ký tự bỏ qua bước sửa AI (giữ chữ Whisper gốc). Chưa mock HTTP nên không test `chat` thật.
- S7: `SwipeCard` luôn trả `false` (`note.card.kt:113`) ⇒ thẻ bật lại, hộp xác nhận hiện, chỉ khi bấm "Xoá" mới `purge`.
- Chưa làm: S1/S2/S10/S12 (Drive), S20 (lost update), S5/S16 (ghi âm), S11 (schema).

## Nhật ký
- 21-10-2026: sửa 3 lỗi + thêm test; chờ kết quả build.
