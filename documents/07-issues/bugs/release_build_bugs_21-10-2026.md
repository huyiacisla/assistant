# Bug — Bản release phát hiện nhiều lỗi

Status: open · Ngày: 21-10-2026 · Phân loại: Jev `07-issues/bugs` 1.00 (auto)
Liên quan: [release_build_signing](../../09-runbooks/release_build_signing_20-10-2026.md) · [phase_10_drive_sync](../../04-phases/phase_10_drive_sync_20-10-2026.md)

## Hiện trạng bằng chứng
Founder báo "nhiều lỗi" ở release, chưa nêu cụ thể. Máy Redmi không nối adb ⇒ **chưa có log**. Dưới đây là soát tĩnh; mục nào chưa tái hiện được ghi rõ.

| ID | Lỗi | Mức chắc | Nguyên nhân gốc | Cách sửa | Trạng thái |
|---|---|---|---|---|---|
| R1 | Đăng nhập Google Drive lỗi trên release | ● cao (suy từ doc, chưa thấy log) | OAuth client Android đăng ký SHA-1 **debug** `99:5A:43:…:CE:82`; release ký bằng `recap-release.jks` ⇒ SHA-1 khác ⇒ Play Services trả `UNREGISTERED_ON_API_CONSOLE` / `DEVELOPER_ERROR` | Founder lấy SHA-1 release, tạo thêm OAuth client Android (cùng `com.huyi.recap`) trên Google Cloud Console | [x] 21/10: tạo client `recap-release` (Android, `com.huyi.recap`, SHA-1 release `A5:68:50:DF:…:70:E3`); chờ Founder thử "Google Drive" trên release |
| R2 | Chuyển giọng (Whisper) không chạy | ● đã biết | Máy thiếu `whisper.bin` (148 MB) sau khi gỡ debug (dữ liệu app bị xoá) | `adb push` lại vào `/sdcard/Android/data/com.huyi.recap/files/` | [ ] |
| R3 | Quyền/nút nổi mất sau `install -r` | ● đã ghi runbook §4 | HyperOS reset POST_NOTIFICATIONS, RECORD_AUDIO, overlay, Trợ năng | Cấp lại theo runbook §4 | [ ] mỗi lần cài |
| R4 | Crash/mất tính năng do R8 | ◐ chưa thấy | Đã sửa ML Kit registrar. Soát: app không dùng reflection (`Class.forName`, `getIdentifier`, Gson) ⇒ rủi ro còn lại ở thư viện | Chờ stack trace + `~/.recap-keys/mapping-v1/mapping.txt` | [ ] cần log |
| R5 | Lỗi khác Founder thấy | ○ chưa biết | — | Founder mô tả hoặc nối adb | [ ] |

## Cách lấy SHA-1 release (R1)
`keytool` bị allowlist chặn trong phiên này. Founder chạy tay:
```bash
keytool -list -v -keystore ~/.recap-keys/recap-release.jks -alias recap | grep SHA1
```
Rồi: Cloud Console → Google Auth Platform → Clients → tạo client Android mới (package `com.huyi.recap`, SHA-1 vừa lấy). Không cần đổi code. Giữ client debug nếu còn build debug.

## Kiểm chứng (bước 6–7)
- [ ] R1: cài release, bấm "Google Drive" ⇒ hiện chọn tài khoản, sao lưu OK
- [ ] R2: giọng → chữ trên release; nếu `UnsatisfiedLinkError` thêm `-keep`
- [ ] R4/R5: nối adb, `adb logcat -b crash` + `retrace` bằng mapping-v1

## Giới hạn
Chưa chạy lại app, chưa sửa code. Không có lỗi nào ở R4/R5 được xác nhận.
