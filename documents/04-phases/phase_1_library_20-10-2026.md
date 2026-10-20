# Phase 1 — Kho (lưu trữ + duyệt + tìm kiếm)

Mục tiêu: có nơi lưu và xem lại mọi thứ đã bắt. Prototype chưa có màn này.

| Việc | Check |
|---|---|
| Thêm Room + KSP (tra version tương thích AGP 9.4.1 / Kotlin 2.4.21 trước) | [x] |
| Entity `Item` (id, loại: ảnh/note/link/text, nội dung, đường dẫn ảnh, nguồn, tạo lúc, folderId, `ai_status`) | [x] |
| Entity `Folder`; tag = chuỗi slug trong `Item.tags` (ponytail: bảng Tag/nối khi cần đếm tag) | [x] |
| FTS cho nội dung item | [x] |
| Repository + Flow | [x] |
| Màn Kho: lưới (`LazyVerticalStaggeredGrid`), lọc theo thư mục/tag | [x] |
| Ô tìm kiếm FTS | [x] |
| Màn chi tiết item: xem, sửa tag/thư mục, xoá + Hoàn tác | [x] |
| Quản lý thư mục thủ công (tạo/đổi tên/xoá/chuyển item) | [x] |
| Thư mục mặc định "Chưa phân loại" | [x] |
| Nút "Mở Kho" trên menu nổi mở đúng màn Kho | [x] |
| 1 test JUnit cho repository (thêm → tìm thấy) | [ ] |

Xong khi: thêm item giả bằng nút debug → hiện trong Kho → tìm được → xoá + hoàn tác được; đóng/mở lại app dữ liệu còn.
Chuẩn tag: slug (`quan-cafe`), hiển thị `#quan-cafe`.

Ghi chú 20-10-2026: Room 2.8.5 + KSP 2.3.12 chạy với AGP 9.4.1/Kotlin 2.4.21. Test máy thật OK: tìm "ca phe" khớp "cà phê" (FTS bỏ dấu), xoá + hoàn tác OK. Còn thiếu: JUnit repository; sửa tên thư mục/mở Kho từ menu nổi chưa test tay.
