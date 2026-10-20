---
name: doc-classify
description: Dùng Jev chọn thư mục đúng trong documents/ (theo documents/README.md) rồi tạo tệp tài liệu mới đúng tên chuẩn. Dùng khi user nói "tạo doc", "ghi lại vào documents", "phân loại tài liệu", hoặc khi sắp tạo tệp .md mới trong documents/.
---

# doc-classify

Mục tiêu: nội dung tài liệu → thư mục `documents/<folder>/` đúng, 1 lần gọi Jev; sau đó thực hiện nội dung theo Workflow Vibecoding.

## Quy trình

| # | Bước | Chi tiết |
|---|---|---|
| 1 | Đọc `documents/README.md` | danh sách thư mục typed (00-guides … 13-backlog) = catalog lớp; không dùng bản nhớ cũ |
| 2 | Tóm nội dung | ≤ 1500 ký tự: tiêu đề + mục đích + vài dòng đầu; không gửi nguyên doc |
| 3 | Gọi `mcp__jev__jev_classify` | `classes` = mỗi thư mục (id = tên thư mục, description = mô tả trong README, thêm "không thuộc: …" khi dễ nhầm) + `manual_review`; `items` = 1 mục/tài liệu (≤ 64) |
| 4 | Xử lý `decision` | `auto` (top ≥ 0.85, margin ≥ 0.5) → tạo luôn · `review` → nêu top-2 + xác suất, hỏi Founder · `manual_review` → hỏi |
| 5 | Tạo tệp | tên `english_snake_case_<DD-MM-YYYY>.md`; có `pnpm flow docs:new` thì dùng, không thì tạo tay theo `documents/template-schema.json` |
| 6 | Báo kết quả | 1 bảng: tài liệu · thư mục · xác suất · quyết định |
| 7 | Thực hiện nội dung doc | nếu doc mô tả việc cần làm (kế hoạch, phase, thiết kế, task) → chạy `documents/00-guides/vibecoding_workflow_20-10-2026.md` cho việc đó: lấy doc vừa tạo làm đầu vào bước 1–4, tick checklist bước con khi xong, chi tiết kỹ thuật theo cột "Chi tiết"; 1 doc = 1 vòng workflow chạy liền, chỉ hỏi Founder ở mục "Chế độ chạy" của workflow |

## Quy tắc

| Quy tắc | Lý do |
|---|---|
| Không di chuyển tệp cũ khi chưa được yêu cầu | `_ai_generation_rules` §2: không vừa chuyển vừa dịch |
| `_analysis`, `11-reference`, `outputs`, `99-archive` không phải lớp phân loại | untyped / đích đặc biệt |
| Ngưỡng 0.85 / 0.5 là mặc định, chưa đo trên dữ liệu thật | chỉnh `auto_accept`, `minimum_margin` sau khi đo |
| Nhầm hay gặp: báo cáo điều tra vs kế hoạch vs kiến trúc | thêm câu loại trừ vào `description` |
| Bước 7 chỉ khi doc là việc cần làm | doc tham khảo/phân tích/báo cáo → dừng ở bước 6 |
| Bước 7 chạm việc khó hoàn tác (xoá dữ liệu, gỡ app) → hỏi Founder trước | theo quy tắc workflow |
| Hạn thiệt hại: sai thư mục chỉ cần chuyển tệp lại | không cần xác nhận cho `auto` |
