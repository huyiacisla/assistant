# Bottom nav 4 tab (Note · Recap · Clipboard · Cài đặt)

| Tab | Nội dung | Ghi chú |
|---|---|---|
| Note | như cũ | — |
| Recap | ảnh chụp/ghi nhận (source≠clipboard, ≠note/link) · chip phân loại theo #tag · DayStrip tuần · nhóm theo buổi · thẻ TimelineRow + thumbnail | "Thống kê hôm nay" → RecapScreen cũ |
| Clipboard | ảnh + link + văn bản · chip loại · nhóm theo ngày · giờ rõ `HH:mm · dd/MM` | nút "Lưu clipboard hiện tại" giữ |
| Cài đặt | Đồng bộ · Thùng rác · Thông báo · Nút nổi | Đồng bộ = xuất JSON (không backend; cắt 200k ký tự) |

Code: `ui/Tabs.kt` (mới), `Timeline.kt` (BottomNav 4 slot, `showDate`, `Thumb`, pill loại), `KhoScreen.kt` (`inPage`, PAGES). Link/Ảnh tab cũ gỡ (nhánh lưới thư mục chết).
Verify: máy thật 3 tab → `assets/nav4_tab{1,2,3}_20-10-2026.png`.

## Bổ sung
- Recap **chỉ xem**: không quẹt xoá, không đổi trạng thái, không sửa; chạm thẻ → `ViewScreen` (ảnh lớn, chữ chọn/chép được); không chọn ngày tương lai (`DayStrip(allowFuture=false)`).
- **Chia sẻ**: nút trong ViewScreen + mục "Chia sẻ" ở menu nhấn giữ thẻ Recap & Clipboard; ảnh qua `FileProvider` (`${applicationId}.share`, `res/xml/share_paths.xml`) kèm chữ.
- Ảnh: `assets/recap_view_20-10-2026.png`, `assets/recap_share_20-10-2026.png`.

## Bổ sung 2
- Recap + Clipboard: không hiện status/giờ nhắc (`noStatus`). Chạm thẻ Clipboard → màn chi tiết sửa nội dung (`tapOpens`).
- Chi tiết: chọn tag = `TagPicker` (chip tag đã chọn, "Tag +" mở bottom sheet: chọn tag có sẵn theo tần suất / tạo tag mới / Xong).
- Nút Quay lại · Xoá · Lưu · Huỷ · Đóng · Xoá lọc · Xoá vĩnh viễn → icon Material (ArrowBack, Delete, Check, Close, FilterAltOff, DeleteForever) ở KhoScreen, RecapScreen, VoiceActivity, Tabs.
- Ảnh: `assets/clip_detail_icons_20-10-2026.png`, `assets/tag_sheet_20-10-2026.png`.

## Bổ sung 3 — AI tự phân loại + nút icon

| Mục | Làm |
|---|---|
| Recap/Clipboard | AI (`aiAnalyze`, DeepSeek) ghi `kind` lần lượt khi `aiStatus=pending`; UI chỉ hiện Pill loại, ẩn tag tay |
| Recap lọc | chip theo loại AI (bỏ chip #tag) |
| Chi tiết Clipboard | bỏ TagPicker, hiện "Loại (AI)" |
| Nút icon | wrapper `IconButton` (ui/Tabs.kt): nền tonal tròn 40dp, icon 20dp; áp mọi nơi |
| Giới hạn | lỗi mạng → `aiStatus=error`, không thử lại; chưa có Jev/WorkManager (Phase 4) |
| Ảnh | assets/ai_kind_recap.png · note_iconbtn.png |

> Khôi phục 20/10 15:22: bottom nav 4 tab bị phiên khác gộp về 2 tab (Note·Cài đặt + autoTag) → trả lại PAGES/inPage/NAV/RecapTab/ClipTab, bỏ autoTag. Ảnh: assets/nav_restored.png

> Tách nút "+": thanh nav = pill 4 trang (nổi, bóng) + nút "+" tròn 60dp riêng bên phải. Ảnh: assets/nav_plus_split.png

> "+" chuyển sang bên trái pill. Ảnh: assets/nav_plus_left.png

> Restyle bottom bar: nền liền màu nền (không hở list), pill viền mảnh không bóng, tab đang chọn = capsule tonal (bỏ chấm), "+" 56dp. Ảnh: assets/nav_restyle.png

> Kính mờ: nền bar trong suốt, list cuộn xuống dưới bar (bottom padding 110dp); pill = blur thật (GraphicsLayer ghi nội dung + BlurEffect 40, tint surface 55%), cần Android 12+. Ảnh: assets/nav_glass.png

## Bổ sung: Nút nổi → toggle trong Cài đặt
- [x] Xoá nút "+ thử" và "Bật nút nổi" khỏi header (KhoScreen)
- [x] Cài đặt: hàng "Nút nổi" dùng M3 `Switch`; bật → `onBubble()` (xin quyền overlay nếu thiếu), tắt → `stopService(OverlayService)`; trạng thái đầu = `OverlayService.running`
- Ảnh: `assets/settings_switch.png`, `settings_switch_on.png`
