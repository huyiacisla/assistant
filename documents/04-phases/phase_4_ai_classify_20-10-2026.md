# Phase 4 — AI phân loại (Jev) + hoàn tác

Mục tiêu: item mới tự vào đúng thư mục + tag, sai thì sửa/hoàn tác dễ.

| Việc | Check |
|---|---|
| Key: `local.properties` → `BuildConfig` (JEV_AI_API_KEY, DEEPSEEK_API_KEY); `.gitignore` đã có; không đọc/in `.env` | [ ] |
| Đọc lại docs Jev (skill `typesafe-ai`): field `systemone`, Choice/Noul, giới hạn | [ ] |
| OkHttp + kotlinx.serialization client cho Jev | [ ] |
| Choice chọn thư mục (+ option `other`) · Noul theo tag | [ ] |
| Quy tắc hành động theo xác suất (giả định: ≥0.7 tự xếp; 0.4–0.7 gợi ý 2; <0.4 → Chưa phân loại) | [ ] |
| WorkManager: hàng đợi `ai_status=pending`, retry có backoff, offline vẫn lưu được | [ ] |
| Hiển thị trạng thái AI trên item (đang chờ / xong / lỗi) | [ ] |
| Bảng log sửa của user (item, thư mục user chọn) để đo độ chính xác | [ ] |
| Đo 30–50 mẫu tiếng Việt thật, chỉnh ngưỡng, ghi kết quả vào `documents/_analysis/` | [ ] |
| DeepSeek làm sạch ghi chú thoại, tóm tắt link (luôn giữ bản gốc) | [ ] |

Xong khi: lưu 10 item hỗn hợp → ≥ 8 vào đúng thư mục hoặc được gợi ý đúng; tắt mạng vẫn lưu, bật mạng tự phân loại.
Lưu ý: cấu trúc kho (tạo/gộp/xoá thư mục) không tự động, luôn hỏi.
