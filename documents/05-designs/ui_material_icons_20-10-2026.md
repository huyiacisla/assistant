# Icon Material thay ký tự/emoji

- Phân loại: Jev `05-designs` 1.00 (auto) · Ngày: 20-10-2026
- Nguồn: `androidx.compose.material:material-icons-extended` (BOM); overlay (View) dùng `res/drawable/ic_close.xml`
- Helper: `ui/Ic.kt` `IconLabel(icon, text, tint)`; `Pill`/`Chip` có tham số `icon`/`trailing`

| Cũ | Icon | Nơi dùng |
|---|---|---|
| ✕ | Filled.Close | xoá ô tìm, đóng sheet, bỏ ảnh, xoá giờ nhắc, Xoá lọc, overlay giữa |
| ⏰ | Outlined.Alarm | chip giờ thẻ timeline, thẻ Kho, sheet nhắc |
| ▾ | Filled.ArrowDropDown | nút Lọc, menu trạng thái |
| ✓ (menu lọc) | Filled.Check (trailing) | 4 mục lọc |
| ✓ / ⏳ / ○ (phân tích) | CheckCircle / HourglassTop / RadioButtonUnchecked | dòng phân tích sheet |
| 📋 🖼 📅 | ContentPaste / Image / CalendarMonth | chip Dán · Ảnh · Ngày giờ |
| 🎤 trong chữ | bỏ, ghi "mic" | gợi ý sheet |

Giữ nguyên: toast "Đã lưu ✓" (thông báo, không phải icon).
Kiểm máy ✓ light: Note tab (alarm, Lọc ▾, menu trạng thái), sheet Ghi chú nhanh ([ảnh](assets/material_icons_quicknote_device_20-10-2026.png)). Chưa kiểm: overlay ✕ giữa, dark, Dán/Ảnh.
