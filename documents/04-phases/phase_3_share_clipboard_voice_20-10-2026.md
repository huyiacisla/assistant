# Phase 3 — Share sheet · Clipboard · Ghi chú thoại

Mục tiêu: các đường vào khác ngoài chụp màn hình.

| Việc | Check |
|---|---|
| Share sheet: nhận text/link/ảnh từ mọi app (intent-filter `SEND`) | [ ] |
| Lưu clipboard: Activity trong suốt, đọc khi có focus, lưu rồi đóng | [ ] |
| Link: lấy tiêu đề + OG tag (OkHttp), lỗi thì lưu link thô | [ ] |
| Ghi chú thoại: `SpeechRecognizer` vi-VN, hiện chữ từng phần (partial) | [ ] |
| Xin quyền `RECORD_AUDIO` | [ ] |
| Lưu bản gốc câu nói + bản đã làm sạch (làm sạch ở phase 4) | [ ] |
| Menu nổi: 3 mục stub → hành động thật | [ ] |

Xong khi: chia sẻ 1 link từ trình duyệt, lưu 1 đoạn copy, nói 1 câu → đều có trong Kho.
Rủi ro: HyperOS có thể chặn đọc clipboard — ghi lại hành vi thực tế; máy có Google nên SpeechRecognizer nên chạy, vẫn spike sớm.
