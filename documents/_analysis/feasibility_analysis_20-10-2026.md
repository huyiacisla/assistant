# Phân tích khả thi — AssistiveTouch AI Recap

Nguồn: `Assistivetouch-Ai-Recap-Prototype.html` (React mock, toàn bộ dữ liệu giả). Ngày: 2026-10-20. Kết luận: **Android làm được**; iOS phải đổi cách kích hoạt.

Ký hiệu: ● làm được · ◐ có hạn chế · ○ khó/rủi ro cao · — không làm được.

## 1. Prototype có gì

| Tác vụ | Mô tả trong demo |
|---|---|
| Chụp & Lưu | chụp màn hình → OCR → tag → lưu Kho |
| Ghi chú nhanh | voice → text → gợi ý thư mục/tag |
| Sao chép link/ảnh | tự bắt khi copy → tóm tắt → phân loại |
| Recap hôm nay | số bài lưu, ý tưởng, thời gian lướt, mood |
| Sắp xếp kho | AI gom nhóm, gợi ý hợp nhất thư mục |
| Nút nổi | kéo thả, chạm mở menu 5 tác vụ, bottom sheet |

## 2. Ma trận khả thi theo nền tảng

| Hạng mục | Android | iOS | macOS/Win | Web/PWA |
|---|---|---|---|---|
| Nút nổi trên mọi app | ● `SYSTEM_ALERT_WINDOW` + foreground service | — | ● always-on-top | — |
| Chụp màn hình 1 chạm | ◐ `MediaProjection` (xin quyền mỗi phiên) | — | ● | — |
| OCR tiếng Việt | ● ML Kit on-device | ● Vision | ● | ◐ tesseract.js |
| Ghi chú thoại | ● `SpeechRecognizer` vi-VN | ● `Speech` | ● | ◐ Web Speech |
| Bắt link/ảnh khi copy | ○ Android 10+ chặn đọc clipboard nền | ○ prompt "Cho phép dán" | ● | ◐ cần focus |
| Thời gian dùng app | ◐ `UsageStatsManager` (quyền đặc biệt) | ○ Screen Time API cần entitlement | ◐ | — |
| Recap từ dữ liệu đã lưu | ● | ● | ● | ● |
| Sắp xếp kho bằng AI | ● | ● | ● | ● |
| Chạy nền ổn định | ◐ OEM kill (Xiaomi, Oppo, Vivo, Samsung) | ◐ | ● | — |

## 3. Android — điều kiện làm được

| # | Điều kiện | Lý do |
|---|---|---|
| 1 | Bỏ "tự bắt khi copy" → "bấm nút, lưu clipboard hiện tại" | OS chặn đọc clipboard nền |
| 2 | Bỏ "thời gian lướt/mood" → recap từ thứ đã lưu | chỉ đo được thời gian foreground theo app, không đo nội dung |
| 3 | Chuẩn bị Play review | overlay · MediaProjection · usage access là quyền nhạy cảm |
| 4 | Hướng dẫn whitelist chạy nền | OEM kill foreground service |
| 5 | OCR + phân loại on-device để giữ claim "không gửi dữ liệu đi" | gợi ý bằng LLM cloud ⇒ phải opt-in + nêu trong chính sách riêng tư |

## 4. iOS — thay thế nút nổi

| Cơ chế | Dùng cho |
|---|---|
| Action Button / Back Tap → App Intent | kích hoạt 1 chạm |
| Share Extension | lưu link/ảnh từ Safari, Instagram |
| `PHPhotoLibrary` observer | phát hiện screenshot mới → OCR → gợi ý tag |
| Control Center widget (iOS 18), Live Activity | điểm chạm gần giống nút nổi |

## 5. Rủi ro

| Rủi ro | Mức | Giảm thiểu |
|---|---|---|
| Play Store từ chối quyền nhạy cảm | cao | tránh AccessibilityService; giải trình use case; spike sớm |
| MediaProjection siết ở Android 14/15 | trung | UX xin quyền rõ; fallback ContentObserver trên MediaStore |
| iOS mất "linh hồn" nút nổi | cao | định vị lại: iOS = Share + Action Button |
| OCR tiếng Việt có dấu kém trên ảnh stylized | trung | chưa kiểm chứng ⇒ test mẫu thật |
| OEM kill dịch vụ nền | trung | màn hướng dẫn whitelist theo hãng |

## 6. Lộ trình đề xuất

| Giai đoạn | Nội dung | Check |
|---|---|---|
| Spike | Kotlin: overlay + foreground service + MediaProjection + ML Kit OCR trên máy thật | [ ] |
| MVP Android | + ghi chú thoại · lưu clipboard bằng tap · Kho Room/SQLite · gợi ý tag rule/embedding nhỏ | [ ] |
| v2 | recap từ dữ liệu đã lưu · sắp xếp kho bằng AI (cloud opt-in) | [ ] |
| iOS | SwiftUI + App Intents + Share Extension + screenshot observer; chung schema/backend | [ ] |

Tránh: Flutter/RN cho phần overlay (vẫn cần native module) · AccessibilityService nếu không bắt buộc.
