# Phase 14 — Nền an toàn dữ liệu

Mục tiêu: chặn mất/ghi đè dữ liệu ở lớp Room, đặt nền để đổi schema và sửa Drive an toàn. Quyết định: [ADR kiến trúc](../02-architecture/adr_architecture_target_21-10-2026.md). Nguồn lỗi: [audit](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md) (S20, S11). Phân loại: `04-phases`.

## Quyết định (bước 1–4)
| Mục | Chọn | Vì sao |
|---|---|---|
| Ghi dữ liệu | Thêm 5 truy vấn `UPDATE` theo cột trong `KhoDao`: `setStatus`, `snooze`, `setAi`, `setFolder`, `saveDetail` | lost update; trigger `item_touch` tự cập nhật `updatedAt` |
| Chỗ thay | `kho.screen` (AI nền, Gom, autosave Chi tiết), `note.screen` (đổi trạng thái), `remind.activity` (Xong, Hoãn) | mọi chỗ `update(copy)` đã tìm thấy |
| Giữ `update(Item)` | Không còn caller; để lại DAO tới khi dọn | rollback dễ |
| Schema | `exportSchema=true` + `ksp { arg("room.schemaLocation", …/schemas) }`; commit JSON | đối chiếu khi đổi version |
| Test | Chưa có DAO test: cần Robolectric hoặc androidTest + máy | ghi rõ, để bước sau |
| `saveDetail` | `reopen` = `status=="open"` của bản trên màn hình; không đụng `kind`, `aiStatus`, `remindCount`, `audioPath` | autosave không còn ghi đè trường Detail không sửa |

## Checklist
- [x] 14.1 DAO: 5 truy vấn theo cột
- [x] 14.2 Thay `update(copy)` ở `kho.screen`, `note.screen`, `remind.activity`
- [x] 14.3 `exportSchema=true` + `schemaLocation` trong `build.gradle.kts`
- [ ] 14.4 `:app:testDebugUnitTest :app:assembleDebug` xanh, `app/schemas/**/9.json` được sinh (xem Nhật ký)
- [ ] 14.5 Máy thật: đổi trạng thái (quẹt, Xong, Hoãn), AI phân loại nền, Gom, sửa Chi tiết — dữ liệu khác không bị ghi đè — chờ Founder
- [ ] 14.6 Test DAO + migration v1→9 (Robolectric hoặc androidTest) — bước tiếp
- [ ] 14.7 Drive gộp thay vì ghi đè + timeout + SHA-1 release — phase sau

## Giới hạn đã biết
- `saveDetail` vẫn ghi cả khối trường Detail cùng lúc; nếu AI đổi `tags` đúng lúc người dùng đang sửa Chi tiết, bản sửa tay thắng (chấp nhận).
- Chưa có test DAO, chưa kiểm trên máy.
- `Item` còn được chèn nguyên (`insert`) khi tạo mới, đúng ý.

## Nhật ký
- 21-10-2026: code xong 14.1–14.3. Build/test chạy nền (kết quả ghi bên dưới khi xong).
