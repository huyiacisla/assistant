# Thẻ Note: tác vụ nhanh · quẹt xoá · thùng rác 30 ngày

- Ngày: 20-10-2026 · Phân loại: `05-designs` · Liên quan: [ui_timeline_bottomnav_reference](ui_timeline_bottomnav_reference_20-10-2026.md) (D16), checklist D17
- Mã: `ui/Timeline.kt` (`TimelineRow`), `ui/KhoScreen.kt` (`del`, `TrashScreen`), `data/Db.kt`

## 1. Tác vụ trên thẻ

| Thao tác | Kết quả |
|---|---|
| Chạm | mở chi tiết (như cũ) |
| Giữ lâu (haptic) | menu nhanh: Chỉnh sửa (mở chi tiết) · Xoá (đỏ) |
| Quẹt trái | nền đỏ + icon thùng rác lộ ra; quá ngưỡng = xoá tạm, thẻ bật lại, danh sách tự bỏ thẻ |
| Xoá trong chi tiết | cùng luồng xoá tạm |

- Nền đỏ chỉ vẽ khi đang quẹt (`dismissDirection == EndToStart`); vẽ thường trực sẽ lộ viền đỏ quanh thẻ nghiêng.

## 2. Xoá tạm (thùng rác)

| Mục | Quy tắc |
|---|---|
| Xoá | `Item.deletedAt = now`; mọi truy vấn `all/inFolder/unsorted/search` lọc `deletedAt IS NULL` |
| Snackbar | "Đã xoá · giữ 30 ngày" + **Hoàn tác** = `restore` |
| Nhắc | xoá ⇒ `cancelRemind`; khôi phục ⇒ đặt lại báo thức nếu `status=open` và `remindAt` chưa tới |
| Giữ | `TRASH_DAYS = 30` |
| Dọn | mỗi lần mở Kho: `expired(now − 30 ngày)` → xoá file ảnh + dòng DB (vĩnh viễn) |
| DB | Room v4→v5: `ALTER TABLE Item ADD COLUMN deletedAt INTEGER` (null = còn dùng) |
| Không đổi | huỷ tức thì từ overlay (VoiceActivity/Clip/Shot "Hoàn tác") vẫn xoá cứng: mục chưa từng hiện trong Kho |

## 3. Màn Thùng rác

- Vào: icon filter → sheet → **Thùng rác** (trái dưới, cạnh Xoá lọc)
- Card (Row): **ô trái 56dp** (ảnh thu nhỏ nếu có; không thì icon loại: ghi âm › ảnh › link › văn bản › ghi chú) · cột giữa: **tiêu đề** (đậm, ≤2 dòng; `title` → dòng đầu text → "Ảnh") · **tóm tắt** (≤2 dòng; `summary` → phần còn lại của text) · **thời gian tạo** `HH:mm · dd/MM/yyyy` + "· còn N ngày" (≤3 ngày: đỏ) · cột phải: nút **Khôi phục** (xanh, `RestoreFromTrash`) trên, nút **Xoá vĩnh viễn** (đỏ, `DeleteOutline`) dưới; mỗi nút 38dp, bo 12dp, nền nhuộm α.12 (`TrashAct`), không còn vòng tròn xám
- Quẹt: phải = khôi phục, trái = xoá vĩnh viễn (giữ nguyên)
- Đóng: ✕ góc trái
- Máy thật 20-10-2026: ● [ảnh](note_trash_list_device_20-10-2026.png) (`feature/kho/trash.screen.kt`, `TrashLead`)

## 4. Kiểm trên máy (Redmi Turbo 4 Pro, sáng)

| Ca | Kết |
|---|---|
| Giữ lâu → menu Chỉnh sửa/Xoá | ● [ảnh](note_quick_menu_device_20-10-2026.png) |
| Quẹt trái → thẻ biến khỏi danh sách (Cần làm 12→11) | ● |
| Xoá trong chi tiết + Hoàn tác | ● thẻ về lại |
| Thùng rác: dòng + "Còn 30 ngày" + Khôi phục | ● [ảnh](note_trash_device_20-10-2026.png) |
| Xoá vĩnh viễn · dọn >30 ngày · Khôi phục đặt lại nhắc | ○ chưa chạy (30 ngày cần đổi giờ máy) |
| Dark mode | ○ chưa xem |
