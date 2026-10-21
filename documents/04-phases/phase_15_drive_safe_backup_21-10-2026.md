# Phase 15 — Drive sao lưu an toàn (S1 · S2 một phần · S10 một phần)

Nguồn: [audit](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md) §5 #2. Phân loại: `04-phases`. Liên quan: [phase_10](phase_10_drive_sync_20-10-2026.md).
Mục tiêu: sao lưu không làm mất bản Drive; khôi phục không làm mất nhắc; HTTP không treo. Chỉ dựa vào code + unit test; **chưa thử trên máy thật**.

## Quyết định
| ID | Lỗi | Cách sửa | Vì sao ngắn nhất |
|---|---|---|---|
| S1 | `backup` ghi đè `recap.json` bằng DB cục bộ ⇒ DB rỗng/máy mới xoá bản Drive; mục đã purge cũng biến mất | `DriveSync.merge(old, local)`: tải bản cũ, giữ mục chỉ có trên Drive, bản cục bộ thắng khi trùng `uid` (hoặc `createdAt:text`); không đọc được bản cũ ⇒ như trước | không đổi định dạng file, không thêm bản xoay vòng |
| S2 | Restore không đặt lại nhắc | sau `dao.insert` gọi `scheduleRemind` cho mục `open`, chưa xoá, `remindAt` tương lai | dùng `scheduleRemind` có sẵn |
| S10 | `HttpURLConnection` Drive không timeout | `connectTimeout 15s`, `readTimeout 60s` ở `call` và `download` | 2 dòng |

## Checklist
- [x] S1 `merge` + dùng trong `backup`
- [x] S2 `scheduleRemind` sau restore
- [x] S10 timeout `call`/`download`
- [x] Test `DriveMergeTest` (2 ca: giữ mục chỉ có trên Drive + local thắng; local rỗng không xoá Drive); thêm `org.json:json` cho test (stub Android ném lỗi)
- [x] `./gradlew :app:testDebugUnitTest :app:assembleDebug` xanh, 8 suite pass (21/10)
- [ ] Máy thật (Founder): Google Drive trên release đăng nhập được (client `recap-release`)
- [ ] Máy thật: sao lưu 2 lần liên tiếp ⇒ `recap.json` trên Drive không mất mục
- [ ] Máy thật: khôi phục ⇒ nhắc tương lai nổ đúng giờ

## Giới hạn đã biết
- Merge không xoá: mục xoá vĩnh viễn trên máy vẫn nằm trên Drive mãi (đổi lại không mất dữ liệu). Chỉ-đọc `deletedAt` mới lan nếu làm hai chiều (chưa làm).
- Restore vẫn chỉ thêm, không cập nhật theo `updatedAt`; chưa bọc transaction; `audioPath`/`remindCount`/`folder` vẫn không nằm trong JSON.
- Chưa xử lý 401 (token hết hạn) và worker thất bại im lặng (D8/D9 trong audit).
- Timeout chưa có ở `DriveAuth.email`.

## Nhật ký
- 21-10-2026: client OAuth `recap-release` đã tạo (SHA-1 release) ở Console; sửa 3 lỗi trên; test xanh.
