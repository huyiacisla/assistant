# Phase 8 — Workflow AI khi tạo note + loại/tag + filter tìm kiếm

Mục tiêu: tạo note → AI phân tích **từng bước, hiện ra từng dòng** → note lưu có giờ nhắc, loại, tag; Kho nhóm được theo loại; thanh tìm kiếm có filter.
Jev phân loại doc: `04-phases` (1.00, auto).

## Quyết định (bước 1–4, mặc định ngắn nhất)
| Mục | Chọn | Vì sao |
|---|---|---|
| Các bước AI | ① Hiểu (recap) → ② Giờ nhắc (`parseWhen` → `aiWhen`) → ③ Loại + tag (`aiAnalyze`, 1 lần gọi DeepSeek JSON) | tái dùng ①② đã có; chỉ thêm ③ |
| Chạy | tuần tự, mỗi bước xong hiện dòng ✓ + kết quả (debounce 1.2s như cũ) | Founder muốn thấy "step by step" |
| Loại (`Item.kind`) | tập cố định: `viec` Việc · `lich` Lịch hẹn · `mua` Mua sắm · `y-tuong` Ý tưởng · `dia-diem` Địa điểm · `hoc` Học tập · `khac` Khác | nhóm ổn định, AI chỉ chọn 1 trong 7 (sai → `khac`) |
| Tag | ≤ 3 slug (`a-z0-9-`), dùng lại cột `Item.tags` | đã có UI + gợi ý thư mục |
| DB | v3 → v4: `ALTER TABLE Item ADD COLUMN kind TEXT NOT NULL DEFAULT ''` | không mất dữ liệu |
| Nhóm Kho | công tắc "Buổi / Loại" ở trang Note | giữ nhóm theo buổi cũ |
| Filter | hàng chip dưới ô tìm: Loại (có trong dữ liệu) + tag top; chạm = lọc, bỏ ngày khi đang lọc; ô tìm khớp cả nhãn loại | |
| Rủi ro | DeepSeek lỗi/offline → bỏ qua bước, lưu `kind=""` (hiện "Chưa phân loại"); không gửi quá 500 ký tự | |

## Checklist
- [x] 1 `Item.kind` + migration v4
- [x] 2 `aiAnalyze` + `parseAnalysis` (kiểm tra chạy được)
- [x] 3 Sheet Ghi chú nhanh: card "Quy trình AI" 3 bước; lưu kind + tag
- [x] 4 Timeline: pill loại + #tag trên thẻ; nhóm theo Buổi/Loại
- [x] 5 Nút "Lọc ▾" cạnh ô tìm → dropdown: Nhóm theo (Buổi/Loại), Loại, Tag, Xoá lọc (hiện "Lọc · n" khi đang lọc)
- [x] 6 Thanh ngày → tuần T2–CN (‹ › đổi tuần), dưới ô tìm; chip filter bên dưới. Đã chụp máy thật; luồng gõ note → 3 bước AI chưa thử trên máy (cần nhập có dấu)

Xong khi: note "mua sữa mai 8h" → ✓ Hiểu, ✓ Nhắc 8:00, ✓ Mua sắm #sua → lưu → Kho nhóm "Mua sắm", chip lọc "Mua sắm" chỉ còn note đó.
- [x] 7 Chọn bất kỳ ngày/giờ khi tạo note: chip "📅 Ngày giờ" → M3 DatePicker → TimePicker 24h (thay dialog hệ thống xấu); `chosen` = `createdAt` của note, thêm nhắc nếu ở tương lai; thanh tuần cho chọn mọi ngày/tuần
- [x] 8 Sửa sheet khi có bàn phím: `imePadding` đặt trước nền; ẩn tay nắm/dòng trạng thái/Dán-Ảnh, mic thu nhỏ → vừa màn hình, không bị cắt đầu. Còn khoảng trống mờ giữa sheet và phím (ime inset HyperOS lớn hơn phím thấy được)
- [x] 9 Thùng rác: card danh sách dạng [ảnh/icon loại] · tiêu đề · tóm tắt · thời gian tạo · icon xoá vĩnh viễn + khôi phục (`trash.screen.kt`); đã chụp Redmi, chi tiết ở [ui_note_trash_30d](../05-designs/ui_note_trash_30d_20-10-2026.md) §3
- [x] 10 Thùng rác: thay 2 nút tròn xám (Material `DeleteForever`/`Restore`) bằng nút tint bo 12dp `TrashAct` (`RestoreFromTrash` xanh + `DeleteOutline` đỏ); chụp lại Redmi
- [x] 11 Khoá dọc: `android:screenOrientation="portrait"` cho cả 5 activity (Main, Capture, Clipboard, QuickNote, Remind); kiểm Redmi: ép `user_rotation=90` → app vẫn `ROTATION_0`
- [x] 12 Bottom nav: thu nhỏ pill 4 tab bên phải (`bottomnav.layout.kt`): cao 56→48dp, mỗi tab rộng cố định 48dp × cao 40dp, icon 24→22dp; pill co theo nội dung, đẩy sát phải bằng `Spacer(weight 1f)`; nút "+" giữ 56dp; chụp Redmi OK
- [x] 13 Bottom nav: nút "+" dời sát pill menu (cả hai nằm bên phải, `Spacer(weight 1f)` đặt trước "+", khoảng cách 10dp); chụp Redmi OK
