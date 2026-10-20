# DESIGN.md — AssistiveTouch AI Recap

Nguồn style: `~/Downloads/Assistivetouch-Ai-Recap-Prototype.html` (iOS 18 Glass). Chi tiết thực hiện: `05-designs/ui_redesign_prototype_20-10-2026.md`. Code: `ui/Theme.kt`, `overlay/OverlayService.kt`, `ui/KhoScreen.kt`.
Mọi UI mới PHẢI theo file này; lệch ⇒ sửa file này trước.

## 1. Nguyên tắc
- **Kính + pastel + tối giản**: nền sáng xám nhạt, thẻ trắng, viền mảnh, 1 điểm nhấn đen.
- **Đen = hành động chính** (nút nổi, nút "Mở Kho", icon banner). Màu accent chỉ ở icon/chip, không tô nền lớn.
- **Một chạm, ý tưởng mới**: ít chữ, mỗi màn 1 việc; tiếng Việt 100%.
- Không phụ thuộc mạng để hiển thị (font nhúng `res/font`).

## 2. Màu
| Vai trò | Hex | Dùng ở |
|---|---|---|
| Nền app | `#F2F2F7` | background |
| Surface | `#FFFFFF` | card, tile, sheet (sheet 95% = `#F2FFFFFF`) |
| Ink | `#000000` | chữ chính, nút chính |
| Ink-2 | `#6B6B78` | chữ phụ, mô tả |
| Line | `#0D000000`–`#1A000000` | viền card / chip / sheet |
| Chip chọn | `#E6E1FF` | FilterChip selected |
| Xanh | `#007AFF` | mic, link |
| Lục | `#34C759` | sao chép, thành công |
| Tím | `#AF52DE` | AI, Recap |
| Cam | `#FF9500` | thư mục |
| Đỏ | `#FF3B30` | camera |
| Gradient kính | `#E6E1FF → #FFD6E8 → #D6F2FF` | nền hero / Recap |
| Pastel tag | `#E6E1FF #FFD6E8 #D6F2FF #E8F5E9 #FEF3C7 #FFEDD5` | tag chip (chọn theo hash tên) |
| Scrim menu | `#66FFFFFF` + blur 20dp | nền menu radial |
| Pill nhãn | `#E6262629` chữ trắng | nhãn dưới tile |

## 3. Chữ — Be Vietnam Pro (400/500/600/700)
| Cấp | sp | weight | Dùng |
|---|---|---|---|
| Hero | 28 | 700 | "Kho" |
| Title | 22 / 16 | 600–700 | tiêu đề màn / sheet |
| Body | 14 / 13 | 400–500 | nội dung card |
| Label | 12 / 11 / 10 | 500 | phụ đề, chip, tag |
- Tiêu đề tracking −0.03em (nếu cần); số liệu dùng tabular.

## 4. Hình khối
- Radius: nút nổi tròn 56dp · tile menu 22 · card 18 · banner 20 · sheet 22 · ô tìm 16 · thumb 12 · chip/tag **full (pill)**.
- Shadow: 2–3 lớp đen .05/.08/.12 (Android: `elevation` 1dp card, 12dp sheet).
- Viền: 1dp `#0D000000` trên card trắng; stroke 3dp trắng quanh nút nổi.
- Khoảng cách: lưới 4dp; padding card 10–14, giữa card 8–12, lề màn 16–24.

## 5. Thành phần
| Thành phần | Quy tắc |
|---|---|
| Nút nổi | tròn đen, viền trắng 3dp, highlight radial (0.3,0.3) `#66FFFFFF→0`; kéo/hít cạnh; chạm = mở menu |
| Menu radial | 5 tile trắng 72dp bo 22 quanh nút giữa "✕" 64dp; r=124dp; nhãn pill tối dưới tile; nền blur 20dp; giữ ✕ = tắt nút |
| Bottom sheet | trắng 95% bo 22, handle 36×5, thumbnail 48×64 (nếu có), tiêu đề 16 bold, phụ đề 12 Ink-2, 2 nút 40dp: **Hoàn tác** (`#F2F2F7`) + **Mở Kho** (đen); tự đóng 5s; KHÔNG blur nền |
| Banner Kho | card trắng bo 20, icon tròn đen 40dp + ⚡ trắng, 2 dòng (14 semibold / 12 Ink-2); chạm = lọc "Chưa phân loại"; ẩn khi 0 mục |
| Card mục | trắng bo 18 viền mảnh; ảnh bo 12; text 13/18 ≤6 dòng; tối đa 3 tag pastel |
| Chip lọc | pill trắng viền mảnh; chọn = `#E6E1FF`, chữ đen, không viền |
| Tab | TabRow nền trong suốt; gạch chân đen |
| Ô tìm | outlined bo 16, placeholder Ink-2 |

## 6. Chuyển động & kính
- Nhấn: scale 0.96 · 120ms. Menu mở: tile trượt từ tâm ra · 180ms ease-out.
- Kính thật chỉ cho cửa sổ overlay (`FLAG_BLUR_BEHIND`, `blurBehindRadius=20dp`); Compose dùng nền bán trong suốt + viền (không `Modifier.blur` cho nền).
- Sheet kết quả không blur để thấy nội dung vừa chụp.

## 7. Cấm
- ✗ Màu tô nền lớn ngoài gradient kính; ✗ nhiều hơn 1 nút đen / khung nhìn.
- ✗ Emoji làm icon chính (dùng vector `res/drawable/ic_*`, 28dp, tint runtime) — ngoại lệ tạm ⚡ ở banner.
- ✗ Dữ liệu demo của prototype (Thảo Điền, 45k cold brew…) trong app.
- ✗ Chữ <10sp; ✗ tap target <40dp.

## 8. Kotlin / Compose (tương thích code)
- Hex trong file = `RRGGBB` (đục) hoặc `AARRGGBB` (8 ký tự, **alpha đứng đầu**, kiểu Android — khác CSS). Kotlin: `Color(0xFFRRGGBB)`; View: `0xFFRRGGBB.toInt()`.
- Đơn vị: `dp` (kích thước/radius/padding), `sp` (chữ). Overlay View: `px(dp) = dp * resources.displayMetrics.density`.
- Nguồn token duy nhất: `ui/Theme.kt` — màu qua `MaterialTheme.colorScheme.*`, chữ qua `MaterialTheme.typography.*`, bo qua `MaterialTheme.shapes.*`; không hard-code màu rải rác (hiện còn `Color(0xFF…)` trong `KhoScreen.kt` ⇒ gom về `Theme.kt`).
```kotlin
object Tok {                                   // ponytail: gom 1 chỗ, thêm khi cần
    val Bg = Color(0xFFF2F2F7);  val Ink2 = Color(0xFF6B6B78)
    val Line = Color(0x0D000000); val ChipOn = Color(0xFFE6E1FF)
    val Blue = Color(0xFF007AFF); val Green = Color(0xFF34C759)
    val Purple = Color(0xFFAF52DE); val Orange = Color(0xFFFF9500); val Red = Color(0xFFFF3B30)
    val Pastel = listOf(0xFFE6E1FF, 0xFFFFD6E8, 0xFFD6F2FF, 0xFFE8F5E9, 0xFFFEF3C7, 0xFFFFEDD5).map(::Color)
}
// radius: 12.dp 18.dp 22.dp = Shapes(small, medium, large); chip/tag = CircleShape
```
- Font: `FontFamily(Font(R.font.be_vietnam_pro_regular, FontWeight.Normal) …)` (`BeVietnam` trong `Theme.kt`); weight Kotlin: 400 `Normal`, 500 `Medium`, 600 `SemiBold`, 700 `Bold`.
- Tương thích: minSdk theo `app/build.gradle.kts`; blur-behind cần API 31+ (máy đích API 36 ✓), guard `Build.VERSION.SDK_INT` nếu hạ minSdk.

## 9. Dark mode (Compose đã làm: `Theme.kt` `dark`; overlay giữ kính sáng)
- Nền `#0E0E10`, surface `#18181B`, ink trắng, line `#1AFFFFFF`; nút chính đảo (trắng/chữ đen). Làm ở bước 9 của doc redesign.
