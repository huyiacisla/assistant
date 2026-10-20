# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
Một người dùng duy nhất: Founder, trên Xiaomi Redmi Turbo 4 Pro (HyperOS 3, Android 16, ROM CN). Dùng hằng ngày, một tay, giữa lúc lướt app khác (Facebook, tin nhắn). Giao diện 100% tiếng Việt.

## Product Purpose
"AssistiveTouch AI Recap": trợ lý cá nhân nằm trên mọi app. Bubble nổi → chụp màn hình/OCR → lưu vào Kho; ghi nhắc bằng giọng nói ("mai 9h họp") → báo thức + popup; xem lại bằng Recap AI; chép/sửa ghi chú dài bằng markdown. Thành công = ghi xong trong vài giây mà không rời app đang dùng, và Kho tự gọn.

## Positioning
AI tự phân loại, Founder chỉ xác nhận: Jev quyết định thư mục/tag, DeepSeek tóm tắt; đổi cấu trúc kho luôn cần Founder duyệt. Một chạm từ bất kỳ app nào (bubble + AccessibilityService). Riêng tư, local-first.

## Operating Context
Ứng dụng cá nhân, cài APK trực tiếp, không phát hành, không thương mại, không backend. Room/SQLite, ảnh lưu file; gọi thẳng Jev + DeepSeek bằng OkHttp (key trong `local.properties`). Clipboard chỉ đọc khi bấm nút. HyperOS giết nền mạnh; cần quyền riêng của Xiaomi (cửa sổ bật lên nền). Nguồn: `documents/_analysis/scope_personal_use_20-10-2026.md`.

## Capabilities and Constraints
- Kotlin native, Jetpack Compose, Material 3; minSdk 26, target/compile 36 (edge-to-edge bắt buộc).
- Job ưu tiên (Founder xác nhận): ghi nhanh khi lướt app khác · nhắc việc bằng giọng nói · Recap AI · ghi chú dài markdown (màn Chi tiết).
- Markdown tự viết, chưa hỗ trợ bảng/link/khối code/danh sách số.
- Recap theo thời gian dùng app (Usage Access): chưa quyết.

## Brand Commitments
Chỉ ràng buộc đã có sẵn trong `documents/DESIGN.md` (kính + pastel + tối giản, đen = hành động chính, Be Vietnam Pro, tiếng Việt 100%); init không mở rộng. Visual world do new-work/document xử lý.

## Evidence on Hand
Ảnh chụp thật từ emulator/Redmi trong `documents/assets/`; QA: `documents/08-qa/`. Chưa có số liệu người dùng ngoài Founder; không bịa.

## Product Principles
1. Một chạm, ít chữ, mỗi màn một việc.
2. AI đề xuất, Founder xác nhận; không tự đổi cấu trúc kho.
3. Lưu local trước, gọi AI sau; dùng được khi mất mạng.
4. Chỉ tối ưu cho một người một máy; không phục vụ đại trà.
