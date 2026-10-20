# Restyle các UI bất ổn (từ QA emulator)

Status: approved · Ngày: 20-10-2026 · Phân loại: Jev `05-designs` 1.00 (auto) · Nguồn: [qa_functional_emulator_pixel9pro_20-10-2026.md](../08-qa/qa_functional_emulator_pixel9pro_20-10-2026.md) B5, B8–B12

## User Problem
| User | Bối cảnh | Kết quả mong muốn |
|---|---|---|
| Founder | Xem timeline Note hằng ngày trên Redmi | Nhãn đọc ngay không đoán; không thừa badge; không nháy viền khi mở sheet; biết cách quẹt |

## Principles
| Rule | Nội dung |
|---|---|
| Token | Chỉ dùng `Tok`/`MaterialTheme` hiện có; không thêm màu mới |
| Tối thiểu | 1 thay đổi = 1 dòng/1 chỗ; không dựng component mới |
| Đo trước khi bỏ animation | B5 chỉ đo được trên Redmi ⇒ chưa đổi code |

## Quyết định
| ID | Hiện trạng | Quyết định | Code |
|---|---|---|---|
| B8a | Nhãn `còn 14g` | `còn 14 giờ` · `n`→`ngày` · `p`→`phút` (cả `trễ`) | `Timeline.kt countdown()` |
| B8b | Badge "Bây giờ" lặp ở header buổi và ở thẻ | Giữ ở header, bỏ ở thẻ | `KhoScreen.kt` L304 |
| B9 | Quả cầu đè đầu thẻ | **Giữ**: là đinh ghim theo design pin-card, đổi màu theo trạng thái là chủ ý | — |
| B10 | Status/nav bar đen khi sheet mở, trắng ở main | Thêm `enableEdgeToEdge()` trong VoiceActivity. **Đã sửa 20-10-2026 (U1)**: theme riêng `Theme.Recap.Sheet` (status/nav bar trong suốt, tắt contrast enforcement) thay `Theme.Translucent.NoTitleBar`; emulator hết đen ([sau](../assets/u_after_sheet_20-10-2026.png)); Redmi chờ Founder xem | `res/values/themes.xml`, `AndroidManifest.xml` |
| B11 | "Note" vs "Recap" khó phân biệt | **Hoãn**: tên tab do Founder đặt; ghi `13-backlog` nếu vẫn nhầm | — |
| B12 | Không gợi ý quẹt | Chú thích "← Xoá · Xong → · chạm = mở · giữ = menu" (12sp, tương phản đủ) + menu giữ có "Hoàn thành/Mở lại" làm thao tác thay thế cử chỉ (U3, [sau](../assets/u_after_main_20-10-2026.png)); chạm thẻ = mở chi tiết (U2/E9, [sau](../assets/u_after_tap_20-10-2026.png)) | `KhoScreen.kt`, `Timeline.kt` |
| B5 | Giật 100% frame trên emulator | **Hoãn**: GPU phần mềm; đo `gfxinfo` trên Redmi trước | — |

## States
| State | Desktop | Mobile | Quyết định |
|---|---|---|---|
| Danh sách trống | — | ● | không hiện chú thích quẹt |
| Có thẻ | — | ● | hiện chú thích |
| Đang lọc | — | ● | không hiện chú thích |

## Verification
| Check | Status |
|---|---|
| Build debug | verified 20-10 |
| Emulator: `còn 14 giờ`, 1 badge "Bây giờ", chú thích quẹt | verified 20-10 (ảnh restyle_main2) |
| Emulator: sheet hết đen thanh trạng thái | **fail** — còn đen, kiểm trên Redmi |
