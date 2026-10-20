# Phân tích tính năng AI — Jev + DeepSeek

> Phạm vi đã chốt: dùng cá nhân, không thương mại. Backend proxy ở mục 4 **không cần**; app gọi thẳng 2 API với key trong `BuildConfig`. Xem [scope_personal_use_20-10-2026.md](scope_personal_use_20-10-2026.md).

Liên quan: [feasibility_analysis_20-10-2026.md](feasibility_analysis_20-10-2026.md) · [tech_stack_analysis_20-10-2026.md](tech_stack_analysis_20-10-2026.md). Ngày: 2026-10-20.

Phân vai theo Founder: **Jev** (`JEV_AI_API_KEY`) = phân loại thư mục, ra quyết định. **DeepSeek** (`DEEPSEEK_API_KEY`) = tổng hợp. Hợp đồng API lấy từ docs chính thức qua tóm tắt tự động (docs.typesafe.ai, api-docs.deepseek.com); kiểm lại field chính xác trước khi code. `.env` chưa được đọc, chưa dùng.

Ký hiệu: ● chính · ◐ phụ/tuỳ chọn · — không dùng.

## 1. Phân vai theo tính năng

| Tính năng | Chạy trên máy | Jev (quyết định) | DeepSeek (tổng hợp) |
|---|---|---|---|
| Chụp & Lưu | ● chụp, OCR | ● chọn thư mục, gắn tag | ◐ tóm tắt ngắn, đặt tên tag mới |
| Ghi chú nhanh | ● voice → text | ● chọn thư mục, gắn tag | ◐ làm sạch/tóm tắt câu nói |
| Sao chép link/ảnh | ● đọc clipboard, lấy tiêu đề | ● chọn thư mục | ● tóm tắt nội dung link |
| Recap hôm nay | ● đếm, % theo thư mục (SQL) | — | ● viết đoạn nhận xét từ số liệu |
| Sắp xếp kho | ● tìm cặp ứng viên (tag trùng) | ● "nên gộp không", chuyển item | ● đặt tên + mô tả thư mục gộp |
| Tìm kiếm | ● FTS local | — | — |

Nguyên tắc: số liệu, đếm, tỷ lệ tính bằng code/SQL, không nhờ LLM. Jev trả xác suất để code quyết; DeepSeek chỉ viết văn bản.

## 2. Jev — thiết kế câu hỏi

Hợp đồng (docs): `POST https://api.typesafe.ai/v1/systemone`, `Authorization: Bearer`, body `model` · `state` · `questions`. Loại câu hỏi: Noul (yes/no, trả xác suất), Choice (chọn 1, tối đa 255 option, trả `probabilities` + `confidence`), Score (thang 1–10 mức). Output token hiện miễn phí, input token tính phí. Các câu hỏi trong 1 request chạy song song.

| Quyết định | Loại | State | Criteria/ghi chú |
|---|---|---|---|
| Item thuộc thư mục nào | Choice | `{text, source, folders[]}` | option `c0..cN` ↔ thư mục, thứ tự cố định; thêm option `other` = "không thư mục nào hợp" |
| Item có tag X không | Noul × mỗi tag | `{text}` | 1 Noul/tag (tag không loại trừ nhau) |
| Có nên tạo thư mục mới | Noul | `{text, folders[]}` | dùng khi Choice trả `other` hoặc confidence thấp |
| Hai thư mục nên gộp | Noul | `{folderA{name,desc,samples}, folderB{…}}` | chỉ hỏi cặp ứng viên do code chọn |
| Item nên chuyển thư mục | Choice | như phân loại | so với thư mục hiện tại |
| Thư mục lồng nhau (sau này) | Choice từng tầng + beam search | | cookbook hierarchical_classification, beam K=3 |

Hành động theo xác suất (ngưỡng là **giả định ban đầu**, phải đo trên dữ liệu thật):

| Điều kiện | Hành động |
|---|---|
| top ≥ 0.7 ∧ top/second ≥ 2 | tự xếp vào thư mục |
| 0.4 ≤ top < 0.7 | xếp tạm + hiện 2 gợi ý đầu để user chọn |
| top < 0.4 ∨ `other` | vào "Chưa phân loại", gợi ý tạo thư mục mới |
| Noul gộp ≥ 0.8 | hiện gợi ý "Gộp?", không tự gộp |

Mọi thay đổi cấu trúc kho (gộp, tạo, xóa thư mục) **cần user xác nhận**; chỉ tự xếp item mới.

Học từ sửa của user: lưu cặp (item, thư mục user chọn) vào Room để đo độ chính xác, và đưa vài ví dụ vào `criteria` ở lần sau.

## 3. DeepSeek — thiết kế tác vụ

Hợp đồng (docs): `POST https://api.deepseek.com/chat/completions`, định dạng OpenAI-compatible, Bearer. Tên model đổi thường (docs hiện ghi `deepseek-flash`, `deepseek-v4-pro`; tên cũ bị retire). **Đặt tên model trong cấu hình backend, không hard-code ở app.** Chưa xác nhận: JSON mode, giới hạn context, rate limit, giá.

| Tác vụ | Input | Output | Kiểm soát |
|---|---|---|---|
| Recap | số liệu đã tính + tiêu đề top item | 2–3 câu tiếng Việt | prompt cấm bịa số; số hiển thị lấy từ SQL, không lấy từ văn bản |
| Tóm tắt link | tiêu đề + đoạn đầu trang | ≤ 2 câu | cắt độ dài input; bỏ qua nếu lỗi |
| Tên + mô tả thư mục gộp | tên + mẫu item của các thư mục | `{name, desc}` | validate JSON ở backend, độ dài tối đa |
| Làm sạch ghi chú thoại | text voice | text đã sửa lỗi, ngắt câu | luôn lưu bản gốc kèm theo |

DeepSeek chỉ sinh văn bản trình bày; không dùng output làm quyết định cấu trúc kho.

## 4. Kiến trúc tích hợp

```
Android (Room, OCR local) → Backend proxy (giữ 2 key) → Jev API · DeepSeek API
```

| Nguyên tắc | Chi tiết |
|---|---|
| Khóa API | **chỉ ở backend** (biến môi trường/secret). Không đưa vào APK, `BuildConfig`, `strings.xml`: APK giải nén được |
| `.env` | giữ ngoài repo/`.gitignore`; không commit; không đưa vào module `app/` |
| Backend | proxy mỏng: validate đầu vào, giới hạn tần suất, gọi 2 dịch vụ, trả JSON gọn. Gợi ý: Cloudflare Worker hoặc Ktor; chọn khi init |
| Xác thực app → backend | token ẩn danh theo cài đặt + Play Integrity khi phát hành |
| Offline-first | Item lưu Room trước (`ai_status = pending`); WorkManager gọi AI khi có mạng; lỗi → giữ `pending`, thử lại có backoff |
| Dữ liệu gửi đi | chỉ text OCR/ghi chú đã cắt độ dài, **opt-in**; không gửi ảnh; lọc số điện thoại/email trước khi gửi (đề xuất) |
| Gộp lời gọi | 1 request Jev chứa Choice thư mục + Noul tag cùng lúc (song song, ít độ trễ) |

Endpoint backend tối thiểu:

| Endpoint | Gọi | Vào | Ra |
|---|---|---|---|
| `POST /v1/classify` | Jev | `{text, folders[]}` | `{folder, probs, tags{}, newFolder?}` |
| `POST /v1/organize` | Jev → DeepSeek | `{folders[] + mẫu}` | `{merges[], moves[]}` + tên/mô tả |
| `POST /v1/recap` | DeepSeek | `{stats, topItems[]}` | `{text}` |
| `POST /v1/summarize` | DeepSeek | `{title, snippet}` | `{summary}` |

## 5. Rủi ro và việc cần kiểm chứng

| Rủi ro | Mức | Cách xử lý |
|---|---|---|
| Lộ khóa nếu nhét vào app | cao | proxy backend; Semgrep gate quét chuỗi khóa |
| Dữ liệu cá nhân (ảnh chụp màn hình có thể chứa tin nhắn) gửi ra ngoài | cao | opt-in, lọc PII, nêu rõ trong chính sách riêng tư và Play Data Safety |
| Dịch vụ bên thứ ba lưu/xử lý dữ liệu theo chính sách riêng (cả 2 nhà cung cấp) | trung | đọc chính sách dữ liệu từng bên trước khi phát hành |
| Ngưỡng xác suất sai với dữ liệu tiếng Việt thật | trung | thu thập mẫu thật, đo, chỉnh |
| Jev phân biệt kém thư mục gần nghĩa ("Inspo Nhà" vs "Ban công nhỏ") | trung | mô tả criteria rõ gồm loại trừ + ví dụ; test cặp dễ nhầm |
| Tên model/giá DeepSeek đổi | trung | cấu hình ở backend; kiểm giá trước khi bật |
| Chi phí tăng khi kho lớn (organize gọi nhiều) | trung | chỉ chạy theo yêu cầu/định kỳ, giới hạn số cặp |
| Phụ thuộc mạng cho tính năng AI | thấp | offline-first, rule local làm fallback |

## 6. Kế hoạch

| Bước | Việc | Check |
|---|---|---|
| 1 | Chốt: Cloudflare Worker hay Ktor cho backend | [ ] |
| 2 | Spike backend: `/v1/classify` gọi Jev với 1 Choice + `other` | [ ] |
| 3 | Test 30–50 mẫu tiếng Việt thật, đo accuracy, chỉnh ngưỡng | [ ] |
| 4 | App: Room `ai_status`, WorkManager, lưu trước gọi sau | [ ] |
| 5 | `/v1/recap` + `/v1/summarize` với DeepSeek | [ ] |
| 6 | `/v1/organize` (Noul gộp + DeepSeek đặt tên), luôn có xác nhận | [ ] |
| 7 | Opt-in + lọc PII + chính sách riêng tư | [ ] |

## 7. Chưa quyết (cần Founder)

| Câu hỏi | Ảnh hưởng |
|---|---|
| Backend: Cloudflare Worker, Ktor, hay hạ tầng khác | triển khai, bí mật, chi phí |
| Có gửi nội dung link đã tải về cho DeepSeek không | quyền riêng tư |
| Ngưỡng tự xếp: thận trọng (hỏi nhiều) hay mạnh tay (tự xếp nhiều) | trải nghiệm |
| Người dùng cuối có phải tự nhập key không | quyết định kiến trúc proxy hay BYOK |
