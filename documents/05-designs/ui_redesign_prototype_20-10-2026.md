# UI Redesign theo Prototype "AssistiveTouch AI Recap" (iOS 18 Glass)

- Nguồn: `~/Downloads/Assistivetouch-Ai-Recap-Prototype.html` (React + Tailwind 3.4, mock tương tác, không backend)
- Ngày: 20-10-2026 · Phạm vi: app cá nhân, Android (Redmi Turbo 4 Pro, API 36)
- Trạng thái: [ ] chưa bắt đầu · Phụ thuộc: Phase 2 xong (capture/clip chạy)

## 1. Tinh thần thiết kế
- Slogan: "Một chạm, ý tưởng mới" · "Lưu ít thôi, nhưng lưu đúng. Còn lại để AI nhớ."
- Phong cách: iOS 18 Glass = kính mờ (backdrop blur 20px) + viền trắng mờ + shadow nhiều lớp; nền pastel gradient; nút nổi có hiệu ứng nhấn lõm.
- 100% tiếng Việt; mock local, không gửi dữ liệu đi.

## 2. Design tokens
| Token | Giá trị |
|---|---|
| Font | Be Vietnam Pro (400/500/600/700), phụ Geist; fallback system |
| Nền app | `#F2F2F7` (--bg), surface trắng |
| Gradient nền kính | `#E6E1FF` → `#FFD6E8` → `#D6F2FF` (br) |
| Accent | xanh `#007AFF` · lục `#34C759` · tím `#AF52DE` · cam `#FF9500` · đỏ camera `#FF3B30`-ish |
| Pastel chip | lục `#E8F5E9` · vàng `#FFFBEB/#FEF3C7` · xanh `#EFF6FF/#DBEAFE` · tím `#EDE9FE` · cam `#FFEDD5` |
| Nền trang demo | sáng `#E8E6E1`/`#F5F3EF` · tối `#0E0E10`/`#18181B` (tham khảo dark mode) |
| Ink | đen `#000`/zinc-900; ink-2 `#6B6B78`; line `rgba(0,0,0,.05–.1)` |
| Radius | nút 56dp tròn · tile menu 20–22 · card 16–20 · chip full · bottom sheet 22+ |
| Cỡ chữ | 10/11/12 (nhãn) · 13/14 (thân) · 22 (tiêu đề) · 28 (hero); tracking −0.03em ở tiêu đề |
| Blur | kính menu/sheet: `blur 20dp`, nền trắng 70–90%, viền trắng 60% |
| Shadow | 2–3 lớp: .05 / .08 / .12 đen |

## 3. Thành phần
### 3.1 Nút nổi (AssistiveTouch)
- Chấm tròn đen 56dp, viền trắng dày, highlight radial 30%/30% (inner shadow = hiệu ứng lõm); kéo thả, hít cạnh; chạm mở menu.
- Hint lần đầu: "Kéo chấm tròn • Chạm để mở menu".

### 3.2 Menu radial (5 tác vụ, quanh nút giữa "×")
| Vị trí | Tile | Màu icon | Nhãn | Hành động app |
|---|---|---|---|---|
| Trên | camera | đỏ | Chụp & Lưu | `ShotAccessibilityService.shoot()` |
| Phải | mic | xanh dương | Ghi chú nhanh | Phase 3 voice→text |
| Trái | folder | cam | Sắp xếp kho | mở Kho + gợi ý AI (Phase 6) |
| Dưới-trái | sparkle | tím | Recap hôm nay | Phase 5 |
| Dưới-phải | link | lục | Sao chép | `ClipActivity` (lưu clipboard) |
- Tile trắng bo 22dp, shadow mềm; nhãn là pill tối (zinc-900/80) dưới tile; nền mờ + blur toàn màn.
- Thay menu dọc hiện tại; thêm "Tắt nút nổi" + "Mở Kho" vào nút giữa (giữ lâu) hoặc hàng phụ.

### 3.3 Bottom sheet kết quả (mỗi tác vụ đổ về đây, kiểu iOS)
- Handle 5dp; kính mờ; toast xác nhận: "Đã lưu ✓", "Đã chuyển voice → text", "Đã lưu lúc 09:40 • Tự động phân loại".
- Chụp: thumbnail + "AI đã đọc chữ trong ảnh" (OCR) + tag tự tạo ("Đã tự động tạo 2 tag").
- Ghi chú thoại: "Ghi chú thoại • Hôm nay 09:41" + "từ giọng nói của bạn".
- Sao chép: link rút gọn; "Tự động bắt khi bạn copy"; "Xem 3 link đã sao chép hôm nay →".
- Hành động: Lưu vào Kho · Mở lại.

### 3.4 Kho
- Banner đầu: "Kho của bạn đang lộn xộn?" + "AI gợi ý gom 12 mục vào 4 thư mục" (icon tia sét nền đen).
- "Gợi ý thư mục": Inspo Nhà / Quán Ăn HCM / Công việc; chip pastel + "+9 khác"; "AI gợi ý: gắn vào …".
- "Gợi ý hợp nhất": "Inspo Nhà" + "Ban công nhỏ" → 1 thư mục lớn hơn?
- Card: nguồn (instagram.com…), tiêu đề, trích văn bản, tag chip; nhóm "AI tự động gom nhóm".

### 3.5 Recap hôm nay
- "AI tổng hợp từ 09:00 – 09:41"; số bài đã lưu; "thời gian lướt" + "Bạn lướt ít hơn hôm qua 18 phút" / "Tiết kiệm 47' lướt / ngày".
- "Bạn đang đào sâu về …": phân bổ chủ đề (70% nhà cửa, 20% quán ăn HCM, 10% outfit).
- "Mood hôm nay: Tò mò & tập trung"; trích dẫn triết lý; "ý tưởng mới", "quán xá", "decor tối giản".

## 4. Ánh xạ sang code hiện tại
| Prototype | Android | File |
|---|---|---|
| Nút nổi | View + GradientDrawable (đã có) → thêm inner shadow/highlight | `overlay/OverlayService.kt` |
| Menu radial + blur | View mới; blur: `WindowManager.LayoutParams.blurBehindRadius` (API 31+) + `FLAG_BLUR_BEHIND` | `OverlayService.openMenu` |
| Bottom sheet | Compose `ModalBottomSheet` trong Activity trong suốt, hoặc overlay View | mới `ui/ResultSheet.kt` |
| Kho (card, chip, banner) | Compose M3 theme tuỳ biến (Be Vietnam Pro, màu §2) | `ui/KhoScreen.kt`, `ui/Theme.kt` |
| Recap | màn Compose mới | `ui/RecapScreen.kt` (Phase 5) |
- Font: nhúng Be Vietnam Pro vào `res/font` (offline, không tải mạng).
- Blur Compose: `Modifier.blur` chỉ blur nội dung; kính thật dùng nền bán trong suốt + viền; blur-behind chỉ cho cửa sổ overlay.

## 5. Kế hoạch thực hiện
- [x] 1. Theme: màu/typography/shape từ §2 (`Theme.kt`), nhúng font
- [x] 2. Nút nổi: viền trắng + highlight lõm
- [x] 3. Menu radial 5 tile + nhãn pill + nền blur (thay menu dọc)
- [x] 4. Bottom sheet kết quả cho Chụp / Sao chép (thay thanh HOÀN TÁC; giữ nút Hoàn tác trong sheet)
- [x] 5. Kho: banner AI, card, chip pastel, giữ 4 tab (Recap/Clipboard/Link/Ảnh copy)
- [x] 6. Ghi chú nhanh (voice→text) — bản local (cần recognizer trên máy)
- [x] 7. Sắp xếp kho — bản local theo tag; gợi ý hợp nhất bằng Jev còn lại (Phase 4/6)
- [x] 8. Recap hôm nay — bản thống kê; tóm tắt AI còn lại (Phase 5, DeepSeek)
- [ ] 9. (menu/tile ✓; còn kéo 60fps, dark mode — xem ui_node_checklist) Kiểm tra trên máy: 60fps kéo thả, menu vừa màn, dark/light, ảnh chụp không lọt overlay

## 6. Chấp nhận
- Menu radial trùng bố cục prototype; 5 tác vụ chạy thật (trừ voice/AI đến phase của chúng).
- Font/màu khớp §2; không phụ thuộc mạng để hiển thị.
- Dữ liệu demo trong prototype (Thảo Điền, 45k cold brew…) chỉ để minh hoạ, KHÔNG nhúng vào app.
