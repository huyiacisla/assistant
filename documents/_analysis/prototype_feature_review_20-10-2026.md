# Rà soát tính năng prototype (đọc lại từ source)

Nguồn: `Assistivetouch-Ai-Recap-Prototype.html` (React mock, đọc từ mã app trong bundle). Phạm vi: Android cá nhân ([scope_personal_use_20-10-2026.md](scope_personal_use_20-10-2026.md)). Ngày: 2026-10-20.

Ký hiệu: ● làm được · ◐ làm được có hạn chế/giá trị thấp · ○ không làm đúng như demo.

## 1. Tổng số

| Cấp | Số lượng | Gồm |
|---|---|---|
| Tác vụ chính (menu tròn) | 5 | Chụp & Lưu · Ghi chú nhanh · Sao chép · Recap hôm nay · Sắp xếp kho |
| Thành phần khung | 3 | nút nổi kéo thả · menu tròn · bottom sheet |
| Chức năng chi tiết | 22 | bảng mục 2 (gồm cả nút nổi, menu tròn) |
| Khả thi | 12 ● · 9 ◐ · 1 ○ | |

## 2. Chức năng chi tiết

| # | Chức năng | Thuộc | Khả thi | Cải thiện / ghi chú |
|---|---|---|---|---|
| 1 | Nút nổi kéo thả, kẹp biên | Khung | ● | thêm hít vào mép, ẩn khi toàn màn hình/camera/game, chỉnh cỡ + độ mờ |
| 2 | Menu tròn 5 mục | Khung | ● | gần mép thì menu tràn ra ngoài màn hình ⇒ mở theo hướng vào trong |
| 3 | Chụp màn hình | Chụp | ◐ | MediaProjection (hỏi quyền mỗi phiên) hoặc Accessibility; thử cả hai |
| 4 | OCR "AI đã đọc chữ" | Chụp | ● | ML Kit; test tiếng Việt có dấu; ảnh không chữ vẫn phải lưu được |
| 5 | Tag tự động | Chụp | ● | Jev Noul theo tag / Choice thư mục |
| 6 | Trích địa điểm ("Thảo Điền") | Chụp | ◐ | cần trích thực thể; chưa kiểm chứng độ chính xác |
| 7 | Lưu vào Kho / Chỉnh tag | Chụp | ● | đổi sang "lưu ngay + Hoàn tác", không bắt xác nhận |
| 8 | Voice → text | Ghi chú | ● | dùng kết quả từng phần (partial) thay vì hiệu ứng gõ giả |
| 9 | Gợi ý thư mục + tag từ giọng nói | Ghi chú | ● | Jev + DeepSeek |
| 10 | Tự bắt khi copy | Sao chép | ○ | Android chặn đọc clipboard nền ⇒ thay bằng Share sheet + nút "Lưu clipboard" |
| 11 | Tóm tắt link, rút gọn link | Sao chép | ◐ | link Instagram/đăng nhập thường không tải được nội dung; dùng OG tag, fallback tiêu đề |
| 12 | "Xem N link đã copy hôm nay" | Sao chép | ● | truy vấn DB |
| 13 | Số bài đã lưu | Recap | ● | SQL |
| 14 | "Ý tưởng mới" | Recap | ◐ | định nghĩa mơ hồ; cần quy tắc (Jev Noul "đây là ý tưởng?") |
| 15 | "Thời gian lướt 47'" | Recap | ◐ | chỉ có thời gian foreground theo app (Usage Access), không phải "lướt"; chọn danh sách app |
| 16 | "Mood hôm nay" | Recap | ◐ | suy từ thứ đã lưu, giá trị thấp; nên bỏ hoặc thu nhỏ |
| 17 | % theo chủ đề | Recap | ● | tính bằng SQL, không nhờ LLM |
| 18 | So với hôm qua | Recap | ◐ | cần lưu lịch sử hằng ngày; phụ thuộc #15 |
| 19 | Gom nhóm kho | Sắp xếp | ● | Jev Choice |
| 20 | Gợi ý gộp + nút Gộp | Sắp xếp | ● | Jev Noul theo cặp; Gộp phải có Hoàn tác |
| 21 | Widget "Đã lưu hôm nay" | Khung (màn hình chính giả) | ◐ | trên Android là App Widget (Glance); không bắt buộc |
| 22 | Banner "Kho lộn xộn?" | Khung | ◐ | cần luật kích hoạt (vd "Chưa phân loại" ≥ N); đưa vào thẻ trong app hoặc thông báo |

## 3. Chưa phù hợp

| Điểm | Vấn đề | Hướng sửa |
|---|---|---|
| Giao diện iPhone ("iOS 18 • Glass", khung máy, thanh trạng thái, dock) | mục tiêu là Android | dùng Material 3, bỏ khung máy giả |
| Tự bắt khi copy (#10) | OS chặn | Share sheet + nút lưu clipboard |
| Menu nổi gồm cả Recap và Sắp xếp | trộn việc "bắt nhanh" với việc "xem/quản lý" | menu nổi: Chụp · Ghi chú · Lưu clipboard · Mở Kho; Recap/Sắp xếp vào app chính |
| Mood, "thời gian lướt" | đo không đúng nghĩa / giá trị thấp | bỏ mood; đổi nhãn thành "Thời gian dùng app" nếu giữ |
| Câu "Tiết kiệm 47' lướt/ngày", "không gửi dữ liệu đi đâu", "60fps" | khẳng định chưa đo, và sẽ gọi API ngoài | bỏ các câu này |
| Xác nhận không thống nhất ("Lưu vào Kho" so với "Đã lưu • Tự động phân loại") | hai luồng khác nhau cho cùng việc | một quy tắc: lưu ngay, có Hoàn tác |
| Tag viết hai kiểu (`inspo-nha` và `#quan-cafe`) | không chuẩn | chuẩn hoá slug + hiển thị `#` |
| Số liệu mock tự mâu thuẫn (hôm nay 12 bài, Kho 73 mục, % theo chủ đề khác tỷ lệ Kho) | gây hiểu lầm khi dựng UI thật | UI thật lấy toàn bộ từ DB |

## 4. Cần bổ sung

| Tính năng | Vì sao | Mức |
|---|---|---|
| **Màn Kho chính**: lưới/danh sách, tìm kiếm FTS, lọc theo thư mục/tag, chi tiết item, sửa/xóa | prototype không có màn duyệt Kho; không có nơi xem lại thứ đã lưu | ● bắt buộc |
| **Share sheet** (nhận link/ảnh/text từ mọi app) | thay cho tự bắt copy, ổn định hơn clipboard | ● bắt buộc |
| Hoàn tác (lưu, đổi thư mục, Gộp) | AI tự xếp có thể sai | ● |
| Quản lý thư mục thủ công (tạo/đổi tên/xóa/chuyển item) | AI không đủ | ● |
| Cài đặt: key, bật/tắt AI, ngưỡng tự xếp, kích thước nút, ẩn nút theo app, quyền | dùng thật cần chỉnh | ● |
| Trạng thái AI (đang chờ/lỗi/offline) trên từng item | app lưu trước, gọi AI sau | ● |
| Xuất/nhập sao lưu (zip DB + ảnh) | gỡ app là mất hết | ● |
| Ngữ cảnh nguồn (app nào đang mở khi chụp) | demo có "Nguồn: Instagram" | ◐ cần Accessibility hoặc Usage Access |
| Quick Settings tile / Action phụ để kích hoạt | thêm cách mở khi nút nổi bị ẩn | ◐ |
| Hỏi Kho bằng ngôn ngữ tự nhiên ("quán cafe nào ở Thảo Điền?") | khớp triết lý "để AI nhớ"; phần "Mở lại" của demo mới là mock | ◐ làm sau, DeepSeek trên vài kết quả tìm được |
| Phát hiện trùng lặp (ảnh/link) | lưu nhiều dễ trùng | ◐ |

## 5. Thứ tự làm đề xuất

| Bước | Nội dung | Check |
|---|---|---|
| 1 | Nút nổi + menu (Chụp · Ghi chú · Lưu clipboard · Mở Kho) | [ ] |
| 2 | Chụp + OCR + lưu Room + màn Kho + tìm kiếm | [ ] |
| 3 | Share sheet + Ghi chú thoại | [ ] |
| 4 | Jev phân loại + Hoàn tác + sửa thủ công | [ ] |
| 5 | Recap (số liệu từ SQL) + DeepSeek viết nhận xét | [ ] |
| 6 | Sắp xếp kho + Gộp có Hoàn tác | [ ] |
| 7 | Sao lưu, cài đặt, các mục ◐ còn lại | [ ] |
