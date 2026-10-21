# Phase 16 — Ghi âm bền, Drive 401, điều hướng menu nổi

Nguồn: [audit](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md) (S5, S16, D8, S18) + tư vấn điều hướng 21/10. Phân loại: `04-phases`.
Chỉ dựa vào code + build + unit test; **chưa thử trên máy thật**.

## Quyết định
| Mục | Lỗi | Cách sửa | File |
|---|---|---|---|
| Ghi âm 60 s | vòng đọc thoát im lặng ở 60 s, UI kẹt "Đang nghe" | sau vòng lặp: nếu `rec === r` ⇒ `runOnUiThread { stopRec() }` (có chốt `rec === r` nên không dừng đôi) | `quicknote.activity.kt` |
| `AudioRecord` | không kiểm `state`, `startRecording()` có thể ném, buffer cố định | buffer `max(1 s, getMinBufferSize)`; `state != INITIALIZED` hoặc `startRecording` lỗi/không ghi ⇒ `release`, báo "Mic không khả dụng", không crash | `quicknote.activity.kt` |
| Mic chạy nền | thiếu `onStop` | `onStop`: nếu đang ghi ⇒ `stopRec()` (giữ phần đã nói; tiếng động ngắn tự bị bỏ) | `quicknote.activity.kt` |
| Drive 401 | worker retry vô ích, không báo | `DriveAuthExpired` ném ở `call`/`download`/`email`; `BackupWorker` đặt prefs `reauth` rồi `failure` (token null cũng vậy); Cài đặt hiện "Hết hạn đăng nhập · bấm để đăng nhập lại"; đăng nhập lại xoá cờ | `DriveSync.kt`, `DriveAuth.kt`, `BackupWorker.kt`, `Tabs.kt` |
| Menu nổi ⇒ Thống kê | màn Thống kê không có đường vào từ menu | thêm mục "Thống kê" gửi `go="recap"` | `menu.view.kt` |
| Giữ state khi `onNewIntent` | `key(nav)` dựng lại cả `KhoScreen`, mất bộ lọc | `go` là state ở `MainActivity`; `KhoScreen(go, goTick)` có `LaunchedEffect(goTick)` chỉ đổi tab/màn, đóng chi tiết đang mở | `main.activity.kt`, `kho.screen.kt` |
| Bỏ nhánh chết | extra boolean `clip` không ai gửi | bỏ tham số `startOnClipboard`; giữ `go="organize"` (chờ phase 6, kho đủ lớn) | `main.activity.kt`, `kho.screen.kt` |

## Checklist
- [x] Ghi âm: dừng 60 s, kiểm `AudioRecord`, dừng khi `onStop`
- [x] Drive 401 + cờ `reauth` + dòng Cài đặt
- [x] Menu "Thống kê"
- [x] `onNewIntent` giữ state; bỏ extra `clip`
- [x] `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease` xanh (21/10, gồm R8 release; cảnh báo cũ không đổi)
- [ ] Máy thật: ghi âm dài >60 s tự dừng; mở micro khi app khác đang dùng mic ⇒ thông báo, không crash
- [ ] Máy thật: thoát app khi đang ghi ⇒ mic tắt, phần đã nói còn
- [ ] Máy thật: menu nổi ⇒ "Thống kê" mở đúng màn; bấm "Recap"/"Sao chép"/"Ghi chú" khi đã đặt bộ lọc ⇒ bộ lọc Note còn nguyên
- [ ] Máy thật: token Drive hết hạn ⇒ dòng "Hết hạn đăng nhập" hiện, bấm lại đăng nhập được

## Giới hạn đã biết
- Không có test đơn vị cho ghi âm/điều hướng (cần thiết bị hoặc Robolectric); chỉ build + soát code.
- Dòng "Hết hạn đăng nhập" đọc prefs lúc vẽ nên cập nhật khi mở lại màn Cài đặt, không tức thời.
- `go="organize"` vẫn không có đường vào bằng giao diện (cố ý).
- Menu nổi giờ 6 mục + Đóng (7 hàng × 52 dp); chưa kiểm vừa màn hình ở mọi chế độ.
- Thoát giữa lúc ghi gọi `stopRec` ⇒ chép chữ chạy nền; nếu activity bị đóng ngay, kết quả có thể bị bỏ.
