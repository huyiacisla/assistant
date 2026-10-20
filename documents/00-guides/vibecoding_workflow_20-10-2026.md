# Workflow Vibecoding

Áp dụng cho mọi tính năng của Recap (personal use). Mỗi vòng = 1 tính năng nhỏ, đóng trong ≤ 1 buổi.
Số bước **không cố định**: bảng tóm tắt dưới là bộ khung mặc định (8 bước); tính năng lớn thêm bước (vd: spike/thử nghiệm, migrate dữ liệu, đo hiệu năng, review chéo), tính năng nhỏ gộp hoặc bỏ bước không cần. Thêm bước mới = thêm 1 hàng ở bảng tóm tắt + 1 nhóm checklist, đánh số lại.
Doc này chỉ nêu **quy trình chung**; chi tiết kỹ thuật nằm ở doc liên quan (cột "Chi tiết"). Bản áp vào kiến trúc/tech stack: [vibecoding_step_by_step](vibecoding_step_by_step_20-10-2026.md).

| # | Bước | Đầu ra | Chi tiết | Jev |
|---|---|---|---|---|
| 1 | Chốt ý | Mục tiêu + tiêu chí xong | `01-product`, `_analysis/scope_personal_use` | `jev_noul` ngoài scope? |
| 2 | Tra tài liệu | Danh sách doc/file liên quan | `03-plans`, `04-phases`, `AGENTS.md` | `jev_rerank`/`jev_find` khi > 5 ứng viên |
| 3 | Thiết kế | Mục thiết kế trong phase/doc | `05-designs`, `DESIGN.md`, `_analysis/tech_stack_analysis` | `jev_compare`/`jev_decide` khi ≥ 2 phương án |
| 4 | Chia nhỏ | Checklist trong phase file | `04-phases/phase_*.md` | `jev_noul` bước độc lập? |
| 5 | Code | Diff nhỏ | doc thiết kế ở bước 3 | — (build/test quyết) |
| 6 | Build & chạy máy thật | Ảnh chụp + log | [build_run_device](build_run_device_20-10-2026.md) | `jev_verify` log khớp kỳ vọng? |
| 7 | Kiểm tra | Test xanh, 0 cảnh báo mới | phase file (tiêu chí xong) | `jev_review`/`jev_audit` diff |
| 8 | Ghi lại & chốt | Doc cập nhật | `04-phases`, `03-plans`, `/doc-classify` | `jev_classify` chọn thư mục doc |

## Bước con (tick khi làm; bỏ mục không áp dụng)

**1. Chốt ý**
- [ ] 1.1 Viết 1–3 câu: làm gì, cho ai
- [ ] 1.2 Tiêu chí "xong" đo được
- [ ] 1.3 Kiểm YAGNI / ngoài scope → bỏ nếu thừa

**2. Tra tài liệu**
- [ ] 2.1 Đọc roadmap + phase hiện tại
- [ ] 2.2 Đọc scope + doc phân tích liên quan
- [ ] 2.3 Tìm code/helper đã có để tái dùng
- [ ] 2.4 Ghi danh sách doc/file liên quan

**3. Thiết kế**
- [ ] 3.1 UI: lấy từ prototype/`DESIGN.md`
- [ ] 3.2 Kỹ thuật: chọn phương án ngắn nhất (skip → reuse → stdlib → native → dep → 1 dòng)
- [ ] 3.3 Ghi rủi ro: dữ liệu, quyền, ROM
- [ ] 3.4 Ghi vào phase/doc thiết kế

**4. Chia nhỏ**
- [ ] 4.1 Tách 3–7 bước, mỗi bước build được
- [ ] 4.2 Xếp theo phụ thuộc
- [ ] 4.3 Đưa checklist vào phase file

**5. Code**
- [ ] 5.1 Làm từng bước nhỏ, giữ style sẵn có
- [ ] 5.2 Không trừu tượng thừa; không đọc `.env`
- [ ] 5.3 Logic không tầm thường → 1 kiểm tra chạy được

**6. Build & chạy máy thật**
- [ ] 6.1 Build
- [ ] 6.2 Cài lên máy + khôi phục trạng thái (nút nổi, quyền)
- [ ] 6.3 Thao tác luồng thật theo tiêu chí bước 1
- [ ] 6.4 Lưu ảnh + log làm bằng chứng

**7. Kiểm tra**
- [ ] 7.1 Chạy test
- [ ] 7.2 Quét bảo mật/lỗi tĩnh
- [ ] 7.3 Soát mất dữ liệu / thiếu validation
- [ ] 7.4 Lỗi → quay lại 5

**8. Ghi lại & chốt**
- [ ] 8.1 Tick checklist phase
- [ ] 8.2 Ghi quyết định + giới hạn đã biết
- [ ] 8.3 Cập nhật roadmap
- [ ] 8.4 Phân loại/tạo doc mới
- [ ] 8.5 Commit khi Founder yêu cầu


Bước thêm (khi cần): **spike** (trước 3), **migrate dữ liệu** (trước 6), **đo hiệu năng** (sau 6), **review chéo** (sau 7). Lệnh/chi tiết của từng mục → doc ở cột "Chi tiết".

## Chế độ chạy: 1 issue = 1 workflow, chạy liền
- 1 issue/tính năng = 1 vòng workflow từ bước 1 đến 8, làm **liền một mạch**, không tách nhiều lượt.
- Hỏi Founder **một lần** ở bước 1–4 (chốt ý + plan). Plan chốt xong → chạy 5–8 không hỏi lại, không xin xác nhận từng bước.
- Gặp điểm mơ hồ nhỏ → chọn phương án mặc định ngắn nhất, ghi quyết định vào phase file (8.2), đi tiếp.
- Chỉ dừng hỏi khi: việc khó hoàn tác (xoá dữ liệu, gỡ app), vượt scope đã chốt, hoặc Jev dưới ngưỡng `auto` mà hai phương án hậu quả khác hẳn nhau.
- Kết thúc bằng 1 báo cáo: đã làm gì, bằng chứng (ảnh/log/test), giới hạn còn lại.

## Quy tắc xuyên suốt
- Chọn bước theo kích thước việc; bỏ bước không có đầu ra thật (YAGNI), không bỏ 6–7 với việc chạm dữ liệu/quyền.
- Vòng 5→6→7 lặp tới khi đạt tiêu chí xong ở bước 1; lỗi thật trên máy > suy đoán.
- Quyết định đổi hướng ghi vào phase file (bước 8), không để trong chat.
- Việc khó hoàn tác (xoá dữ liệu, gỡ app) hỏi Founder trước.
- Một máy chỉ một phiên điều khiển tại một thời điểm.
- Trả lời Founder: tiếng Việt, ngắn, bảng/checklist.
- Không ghi mã/lệnh cụ thể vào doc này; thêm vào doc chi tiết rồi link.

## Jev (System One)
- Phán đoán có xác suất cho việc ngữ nghĩa mà code thường không quyết được; build/test/lint vẫn là nguồn chân lý.
- Ngưỡng `auto` ≥ 0.85, margin ≥ 0.5 (chưa đo trên dữ liệu thật); dưới ngưỡng → hỏi Founder.
- Không gửi `.env`, khoá hay dữ liệu cá nhân trong Kho; chỉ tóm tắt ≤ 1500 ký tự.

## Mẫu 1 vòng
Bộ khung mặc định: chốt ý → tra doc → thiết kế → chia bước → code → chạy máy thật → kiểm tra → tick phase (thêm/bớt tuỳ việc). Ví dụ cụ thể: xem [ui_redesign_prototype](../05-designs/ui_redesign_prototype_20-10-2026.md).
