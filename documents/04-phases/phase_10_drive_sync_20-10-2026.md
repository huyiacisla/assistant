# Phase 10 — Sao lưu Google Drive (một chiều)

Mục tiêu: Recap → Drive của chính Founder (Gmail cá nhân), không backend. Phân loại doc: `04-phases` (kế hoạch/phase).

## Quyết định
| Mục | Chọn | Vì sao |
|---|---|---|
| Hướng | Một chiều lên + khôi phục thủ công | 1 thiết bị, tránh xung đột |
| Quyền | `drive.file` | chỉ thấy tệp do app tạo |
| Đăng nhập | Play Services `AuthorizationClient` | không cần client secret |
| Gọi API | REST `HttpURLConnection` | cùng kiểu `capture/Ai.kt`, 0 dep thêm |
| OAuth client | Android: `com.huyi.recap` + SHA-1 debug `99:5A:43:7E:78:B7:87:B0:F1:1A:46:88:93:E1:3B:E3:19:62:CE:82` (client `recap`) | Founder tạo trên Google Cloud Console |
| OAuth client (release) | Android: `com.huyi.recap` + SHA-1 release `A5:68:50:DF:F8:4F:87:53:42:61:7B:54:83:05:A3:0C:27:32:70:E3` (client `recap-release`, 21/10) | mỗi khoá ký cần một client riêng; không đổi code |

## Checklist
- [x] 10.0 Founder: Cloud project + bật Drive API + màn hình đồng ý (thêm Gmail làm test user) + OAuth client Android (thông số trên)
- [x] 10.1 Dep `play-services-auth` + `sync/DriveAuth.kt` (xin token, hỏi tên tài khoản qua Drive `about`)
- [x] 10.2 Cài đặt: hàng "Google Drive" Kết nối / Đã kết nối + email
- [x] 10.3 Thử máy thật: đăng nhập thành công, hiện email (cần 10.0)
- [x] 10.4 Tải `recap.json` + ảnh lên thư mục "Recap"
- [x] 10.5 Nút "Khôi phục từ Drive" (nhập, bỏ mục trùng)
- [x] 10.6 `updatedAt` + id ổn định (migration Room) — chuẩn bị hai chiều
- [x] 10.7 WorkManager định kỳ (gộp Phase 4)

## Nhật ký
- 20-10-2026: build + cài OK. Bấm "Google Drive" → Google trả `UNREGISTERED_ON_API_CONSOLE` (đúng dự kiến: chưa có OAuth client Android) → luồng gọi tới Play Services chạy. Ảnh: `assets/settings_drive.png`, `assets/drive_auth_try.png`. Chờ 10.0 để thử 10.3.
- 20-10-2026 15:46: có OAuth client → bấm "Google Drive" hiện hộp "Chọn tài khoản" (Recap yêu cầu quyền truy cập). Ảnh `assets/drive_auth_try2.png`. Chờ Founder tự chọn tài khoản + đồng ý để tick 10.3.
- 20-10-2026 15:48: chọn dohuuhuy0@gmail.com → `403 access_denied` (app ở chế độ Testing, tài khoản chưa là Test user). Sửa ở Console: Google Auth Platform → Đối tượng → Người dùng thử → thêm email. Ảnh `assets/drive_err.png`.
- 20-10-2026 16:0x: đã thêm Test user → hết 403; Google hiện "chưa xác minh ứng dụng" (bình thường ở chế độ Testing) → Founder bấm Tiếp tục + cấp quyền Drive. Ảnh `assets/drive_auth_try3.png`.
- 20-10-2026 15:53: 10.3 xác nhận 'Đã kết nối · dohuuhuy0@gmail.com' (assets/drive_connected.png). 10.4 `sync/DriveSync.kt`: thư mục Recap, recap.json ghi đè, ảnh bỏ qua nếu đã có; bấm hàng Google Drive khi đã kết nối = sao lưu. Chạy trên máy: toast + 'sao lưu 20/10 15:53' (assets/drive_backup.png). Chưa đối chiếu file trên drive.google.com.
- 20-10-2026 15:55: 10.5 `DriveSync.restore` + hàng 'Khôi phục từ Drive' (hiện khi đã kết nối). Gộp theo khoá createdAt+text, tải ảnh thiếu vào filesDir; folderId không sao lưu ⇒ về chưa phân loại. Máy thật: tải recap.json OK, toast 'Đã khôi phục 0 mục mới, 0 ảnh' (đúng: mọi mục đã có; xác nhận file có trên Drive). Ảnh assets/drive_restore.png. Chưa thử trường hợp thêm mục thật (cần xoá cứng 1 mục).
- 20-10-2026 16:0x: 10.6 Item thêm `uid` (UUID) + `updatedAt` (trigger SQL item_touch tự cập nhật khi UPDATE); Room v5→v6 backfill uid=randomblob, updatedAt=createdAt; backup/restore dùng uid (bản cũ không uid ⇒ khoá createdAt+text). Kiểm tra DB máy thật (kéo kho.db, sqlite3): 39 mục/39 uid khác nhau, trigger có, UPDATE làm updatedAt tăng, user_version=6. Chưa làm: ghi đè theo updatedAt (hai chiều) — thuộc bước sau.
- 20-10-2026 16:0x: 10.7 `sync/BackupWorker` (WorkManager 2.10.2, định kỳ 1 ngày + cần mạng; bật ⇒ chạy ngay 1 lần). Công tắc 'Tự động sao lưu' hiện khi đã kết nối; sao lưu cả mục trong thùng rác (allRows). Token nền im lặng nếu đã cấp quyền; hết hạn (Testing 7 ngày) ⇒ lượt thất bại, bật lại bằng tay. Máy thật: bật công tắc ⇒ prefs auto=true, last cập nhật ngay (worker chạy OK). Hạn chế: dòng 'sao lưu …' trên màn Cài đặt chỉ làm mới khi mở lại. Ảnh assets/auto_on.png.
