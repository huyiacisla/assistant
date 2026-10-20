# Material 3 — màn chi tiết (20-10-2026)

| Màn | Thay đổi | Ảnh |
|---|---|---|
| Chi tiết | Scaffold + TopAppBar (back, xoá, FilledIconButton lưu); Card ảnh 22dp; **chỉ còn Tag** (bỏ chip thư mục + chip AI + dòng "Loại (AI)") | assets/m3_detail_device_20-10-2026.png |
| Recap hôm nay | TopAppBar, hero 28dp, FilledTonalButton Sao chép + Button Chia sẻ, LinearProgressIndicator, Panel Card 22dp | assets/m3_recap_device_20-10-2026.png |
| Thùng rác | TopAppBar, Card item, FilledTonalButton "Khôi phục" | chưa chụp |

Giới hạn: FolderDialog giữ nguyên; dark mode chưa kiểm; Thùng rác chưa chụp (trống). Dữ liệu `folderId` giữ nguyên khi lưu (chỉ không sửa được ở Chi tiết).

## Cập nhật: Clipboard sửa được
- Detail: Tag nằm trên Nội dung.
- Tab Clipboard: chạm thẻ → `DetailScreen` (sửa Nội dung/Tag, Lưu, Xoá). Tab Recap vẫn `ViewScreen` chỉ đọc. Chia sẻ qua `onShare` trên thẻ.
- Kiểm máy: mở được, ô Nội dung focus + bàn phím ✓. Chưa kiểm: bấm Lưu và dữ liệu giữ lại.
- Ảnh: assets/m3_detail_clip_device_20-10-2026.png
