# Phase 12 — Hiển thị từng bước AI khi chụp màn hình (20-10-2026)

## Yêu cầu
"show step by step ai phân tích xử lý cap màn hình"

## Quyết định (ponytail)
- Card kính cuối màn hình `OverlayService.showSteps(labels)` → trả hàm `update(n)`; ✓ xong · … đang chạy · ○ chờ; `-1` = gỡ (tự gỡ sau 20s nếu kẹt).
- `saveShot` (Shot.kt) gọi từng bước: ① Chụp → ② Đọc chữ (OCR) → ③ AI hiểu (`aiRecap`) → ④ Phân loại (`aiAnalyze` → kind + tags) → ⑤ Lưu. Xong thì thay bằng undo bar, dòng phụ = recap + loại.
- Ảnh không chữ: bỏ qua AI. AI lỗi: `aiStatus="error"` (KhoScreen tự thử lại).
- Không đổi DB / không thêm dependency.

## Checklist
- [x] showSteps card
- [x] saveShot tích hợp 5 bước + lưu kind/tags/aiStatus
- [x] build + install + chụp qua bubble trên máy thật (card hiện ✓ ✓ ✓ … ○)
- [x] 0 FATAL logcat

## Bổ sung: collapse text OCR trong chi tiết Recap
- `ViewScreen` (Tabs.kt): text > 120 ký tự hoặc ≥ 3 xuống dòng → mặc định 3 dòng + "Xem thêm" / "Thu gọn" (state theo item.id). Vẫn chọn/chép được.
- [x] build + install + kiểm tra trên máy: hiện 3 dòng + "Xem thêm", bấm mở rộng, 0 FATAL.

## Bổ sung: title + summary lên trên ảnh
- `TimelineRow` (Timeline.kt): thứ tự panel = title → summary (rest) → ảnh (trước đây ảnh đứng đầu).
- [x] build + install + kiểm tra tab Recap trên máy: title/summary nằm trên, ảnh dưới, 0 FATAL.

## Bổ sung: card chỉ còn tiêu đề · tóm tắt · ảnh · giờ · số thứ tự · tag
- `TimelineRow`: bỏ chip loại (kind "Khác/Việc…") và chip kiểu (Ảnh/Link/Văn bản); tag hiện cả ở Recap (trước đây ẩn khi `noStatus`).
- Giữ nguyên chip nhắc/trạng thái ở tab Note.
- [x] build + install + kiểm tra tab Recap, 0 FATAL.

## Bổ sung: AI đặt title + summary cho ảnh chụp
- Room v9 (Migration 8→9): thêm `Item.title`, `Item.summary` (mặc định ""). DriveSync xuất/nhập 2 trường.
- `aiBrief(text)` (Ai.kt): 1 call JSON {title ≤8 từ, summary ≤40 từ} thay `aiRecap` ở bước ③ của `saveShot`.
- Card (`TimelineRow`) + chi tiết (`ViewScreen`): ưu tiên title/summary AI, rỗng → fallback dòng đầu / phần còn lại của text OCR (item cũ không đổi).
- [x] build + install (migration chạy, app mở không crash, 0 FATAL)
- [ ] chụp thử end-to-end sau thay đổi: CHƯA — lúc thử máy đang ở app khác (Facebook), dừng không bấm tiếp.

## Fix: "Mở Kho" + "Chụp & Lưu" không chạy khi đang ở app khác (Facebook/Zalo)
- Nguyên nhân (giả thuyết, chưa xác nhận trên máy): overlay bị gỡ TRƯỚC `startActivity` → Android chặn mở Activity từ service nền. Trong Recap thì vẫn chạy vì app đang foreground.
- Sửa: `act()` trước, `close()/closeMenu()` sau (undoBar `btn`, menu tile).
- Dịch vụ Trợ năng chưa bật → luôn đi đường MediaProjection (CaptureActivity, hộp thoại mỗi lần). Bật Trợ năng Recap để chụp 1 chạm.
- [x] build + install
- [ ] Founder test: Facebook → bubble → Chụp & Lưu; thanh kết quả → Mở Kho
- Nguyên nhân thật (logcat): CaptureActivity chạy trong task của Recap (baseActivity=MainActivity) → kéo Recap lên trước app đang chụp → ảnh ra Recap/hộp thoại, không phải Zalo. Sửa: `taskAffinity=""` cho Capture/Clip/Voice Activity.
- Mở Kho → mở thẳng tab Recap (extra go=recap + CLEAR_TOP, như menu 'Recap hôm nay').
