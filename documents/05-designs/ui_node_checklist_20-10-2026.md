# Checklist node UI — áp style prototype (iOS 18 Glass)

- Nguồn style: `~/Downloads/Assistivetouch-Ai-Recap-Prototype.html` → `documents/DESIGN.md` (token) → `ui/Theme.kt` (`Tok`)
- Ngày: 20-10-2026 · Phân loại: Jev `05-designs` 0.75 (review; thay thế `06-tasks` 0.25)
- Cách dùng: làm theo thứ tự; mỗi node đạt khi khớp DESIGN.md §5 + kiểm trên máy (Redmi Turbo 4 Pro)

## A. Nền tảng
- [x] A1. Token màu `Tok` + `RecapTheme` (Theme.kt)
- [x] A2. Font Be Vietnam Pro nhúng `res/font`
- [x] A3. Shapes 12/18/22, chip pill
- [x] A4. Dark mode Compose (theo hệ thống; đã kiểm Kho) — overlay giữ kính sáng
- [x] A5. Hex overlay đã qua `Tok` (còn Color.BLACK/WHITE = ink/surface)

## B. Overlay (View thuần, WindowManager)
- [x] B1. Nút nổi: kính mờ (tint đen 20% + gradient trắng 60%→20% + viền trắng 2dp + highlight radial); không dùng blur-behind vì cửa sổ 56dp sẽ blur hình vuông. Đã build + cài; chưa chụp ảnh máy (adb tap bị chặn, Founder bật nút nổi bằng tay)
- [x] B2. Menu radial: nền blur, ✕ giữa 64dp
- [x] B3. 5 tile trắng 72dp bo 22 + nhãn pill (Chụp & Lưu · Ghi chú nhanh · Sao chép · Recap hôm nay · Sắp xếp kho) — đã kiểm trên máy; spacing + nhãn khớp prototype; icon outline (stroke 1.75)
- [x] B4. Hiệu ứng nhấn scale 0.96 / 120ms
- [x] B5. Bottom sheet kết quả: handle, thumbnail, 2 dòng, Hoàn tác + Mở Kho, tự đóng 5s, không blur
- [x] B6. Giữ ✕ = tắt nút (đã kiểm trên máy)
- [x] B7. Kéo + hít cạnh ✓ (đã kiểm); 60fps không đo được (gfxinfo không đếm khung overlay); ảnh chụp không lọt overlay: chấp nhận theo code (ẩn bubble 250ms trước `shoot()`), chưa kiểm trên máy do session song song chiếm adb

## C. Màn hình app (Compose)
- [x] C1. Header "Kho" + pill đen "Bật nút nổi"
- [x] C2. TabRow 4 tab, gạch chân đen
- [x] C3. Banner AI (icon đen ⚡) → mở "Sắp xếp kho"
- [x] C4. Ô tìm bo 16
- [x] C5. Chip lọc pill, chọn = `#E6E1FF`
- [x] C6. Card mục bo 18 + tag pastel
- [x] C7. Dialog "Sắp xếp kho" (gom theo tag)
- [x] C8. Màn Recap hôm nay (gradient pastel, thống kê, top tag)
- [x] C9. Icon vector `ic_bolt` thay ⚡
- [x] C10. Clipboard là URL (http/https/www, cả đoạn) ⇒ `type=link` ⇒ tab Link, không lẫn tab Clipboard (`Clip.kt` `isLink`, test `ClipLinkTest`; chưa copy thật trên máy)
- [x] C11. Màn chi tiết mục: hiện ảnh (card bo 18, cuộn được), nút Lưu pill đen, Xoá đỏ, ô bo 16, chip thư mục pill (đã kiểm sáng; dark chưa)
- [x] C12. Recap hôm nay: nút Sao chép (clipboard + toast) + Chia sẻ (ACTION_SEND text/plain) — đã kiểm trên máy

## D. Màn mới
- [x] D1. VoiceActivity (ghi chú thoại) — build xong; chưa chứng minh nhận dạng giọng trên ROM CN
- [x] D4. Sheet "Ghi chú nhanh" theo prototype: handle, mic xanh + trạng thái (Đang nghe… / Đã chuyển voice → text), thẻ "Ghi chú thoại • Hôm nay HH:mm", ô gõ tay, nút Lưu note đen → Đã lưu ✓ + Hoàn tác (đã kiểm trên máy, gõ tay, mic bị từ chối; lưu → hiện đầu Kho). Hoãn: banner tag AI + "AI gợi ý: gắn vào…" (Phase 5/6); dark chưa xem
- [x] D4. Nhắc nhở từ ghi chú: `parseWhen` (VN: 3h chiều mai · sau 30 phút · thứ sáu · 20/11) → chip ⏰ trong sheet → `Item.remindAt` (Room v2, migration) → AlarmManager + notification; thẻ Kho hiện ⏰ (test `RemindTest` ✓, migration đã mở trên máy; chưa thử bắn thông báo thật; mất khi reboot)
- [ ] D2. Gợi ý hợp nhất (cần Jev, Phase 4/6)
- [ ] D3. Tóm tắt AI trong Recap (DeepSeek, Phase 5)
- [x] D5. AI (DeepSeek, Phase 4) hiểu giờ phức tạp hơn `parseWhen` ("cuối tuần sau", "trước Tết") — `capture/Ai.kt`, gọi khi luật trả null; test parse ✓; API thật ✓ ("cuối tuần sau" → 31/10 08:00, "mua sữa" → null); app đã cài, chưa thử UI trên máy
- [x] D6. Ghi chú nhanh: mic là node chính (nút 88dp, pulse khi nghe), text phụ (hiện khi có chữ / chọn "Gõ tay"); chỉ định dịch vụ Google Recognition (máy CN không có mặc định), lỗi mic hiện rõ + tự chuyển gõ tay; AI recap "🤖 Hiểu: …" sau khi nghe — build ✓, chưa thử mic trên máy thật
- [x] D7 [x] Voice: `SpeechRecognizer` hỏng trên ROM CN; Gboard mic app không kích hoạt được; Vosk small-vn bị loại (sai nhiều) → whisper.cpp `small q5_1` offline (NDK/CMake trong app, arm64; source `third_party/whisper.cpp`; model KHÔNG trong APK, adb push → `files/whisper.bin` ~190MB). Nút mic xanh: bấm = ghi, chữ hiện live ~0.8s/lần, bấm lại = chốt. Đã xác nhận Whisper ra chữ đúng + live; recap/chip ⏰/lưu note → Kho (timeline) chạy
- [x] D8. Kho: thêm tab "Note" đứng trước Recap (Note · Recap · Clipboard · Link · Ảnh copy); Note = type note ngoài clipboard, Recap không còn chứa note — build ✓, cài máy ✓ (ảnh s5). Mic Gboard trên máy: "Đang nghe…" + chấm mic xanh ✓; nút xanh trong sheet chỉ mở bàn phím
- [x] D9. Note theo ngày + timeline thẻ (DayStrip, TimelineRow) và bottom nav 5 icon + "+" giữa — build ✓, cài máy ✓ (sáng); chi tiết [ui_timeline_bottomnav_reference](ui_timeline_bottomnav_reference_20-10-2026.md); "+" mở Ghi chú nhanh (VoiceActivity) ✓ trên máy; chưa kiểm dark
- [x] D10. Ghi chú nhanh thêm Dán · Ảnh · chip giờ nhắc · tự nhận link ([ma trận](ui_quicknote_actions_20-10-2026.md)) — kiểm máy ✓ chip giờ + tự nhận link; chưa kiểm Dán, Ảnh
- [x] D11. Header Note (tìm riêng) + thẻ timeline đơn sắc + accent tím nhạt ([ghi chú](ui_timeline_bottomnav_reference_20-10-2026.md)) — kiểm máy ✓ light; chưa kiểm dark
- [x] D12. Thẻ Note 7 trường: tiêu đề · nội dung · giờ tạo · giờ nhắc · Nhắc lần N · trạng thái tint (menu) · nhóm 4 buổi Sáng/Trưa/Chiều/Tối, DB v3 ([spec](ui_timeline_bottomnav_reference_20-10-2026.md)) — kiểm máy ✓ light (menu + Hoàn thành); chưa kiểm dark, "Nhắc lần N" thực tế
- [x] D13. Thẻ Note kiểu "vé": vạch + wash tint trạng thái, đường xé, đếm ngược nhắc ([spec](ui_timeline_bottomnav_reference_20-10-2026.md)) — kiểm máy ✓ light (thẻ Hoàn thành); chưa kiểm dark, chip còn/trễ
- [x] D14. Icon Material thay ký tự/emoji toàn project ([bảng](ui_material_icons_20-10-2026.md)) — build ✓, kiểm máy ✓ light (Note, sheet); chưa kiểm overlay ✕, dark
- [x] D14. Thanh lọc trạng thái Note dạng segmented (Cần làm · Hoàn thành · Chờ nhắc + số) ([spec](ui_timeline_bottomnav_reference_20-10-2026.md)) — kiểm máy ✓ light (Hoàn thành); chưa kiểm dark
- [x] D15. Thẻ Note giữ nghiêng ±1° + hiệu ứng lắc khi nhấn (lò xo nảy) theo ý Founder; chạm → mở chi tiết ✓ trên máy (đã thử bỏ lắc, Founder yêu cầu giữ lại)
- [x] D16. Màn chi tiết theo Material 3 (Detail / Recap / Thùng rác); Detail chỉ còn Tag, bỏ phân loại ✓ trên máy → ui_material3_detail_screens_20-10-2026.md
- [x] D16. Thẻ Note: chạm không còn mở chi tiết (chỉ lắc); mở chi tiết qua giữ lâu → Chỉnh sửa
- [x] D17. Menu giữ lâu thẻ Note làm mới: khung bo 20 + viền + bóng, icon trong chấm tint, vạch chia trước Xoá
- [x] D16 Recap/Clipboard AI tự phân loại (chỉ hiện loại) + IconButton tonal
- [x] D18. Chi tiết Clipboard chỉnh sửa được: tab Clipboard mở `DetailScreen` (Tag trên Nội dung, sửa + lưu) thay `ViewScreen`; Recap vẫn chỉ đọc — kiểm máy ✓ (mở, focus + bàn phím; chưa bấm lưu) → m3_detail_clip_device_20-10-2026.png
