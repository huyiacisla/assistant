# Chi tiết note: redesign + nhập Markdown

Status: approved · Ngày: 20-10-2026 · Phân loại: Jev `05-designs` (cùng chủ đề [ui_material3_detail_screens](ui_material3_detail_screens_20-10-2026.md)) · Nguồn: Founder "trang chi tiết quá xấu, bổ sung input text dạng markdown được không"

## User Problem
| User | Bối cảnh | Kết quả mong muốn |
|---|---|---|
| Founder | Mở chi tiết 1 note trên Redmi để đọc/sửa | Trang gọn, có thứ bậc; gõ ghi chú có định dạng (đậm, tiêu đề, danh sách, việc cần làm) |

## Principles
| Rule | Nội dung |
|---|---|
| Token | Chỉ `MaterialTheme`/`Tok` hiện có; không thêm màu |
| Ponytail | Markdown tự viết, không thêm thư viện; ceiling ghi bằng `// ponytail:` |
| Gesture thay thế | Nút toolbar chèn ký hiệu — không bắt gõ tay `**` trên bàn phím điện thoại |
| Touch | Nút toolbar `IconButton` 48dp; chip Sửa/Xem ≥ 48dp |

## Quyết định
| ID | Hiện trạng | Quyết định | Code |
|---|---|---|---|
| D1 | Chỉ 1 ô `OutlinedTextField` thô | Khung biên tập: Card 22dp, chip **Sửa / Xem**, toolbar cuộn ngang, ô gõ không viền | `KhoScreen.kt DetailScreen` |
| D2 | Không rõ ngày giờ/nhắc | Đầu trang: giờ · nhãn ngày, giờ nhắc (xanh); tiêu đề 24sp Bold | `KhoScreen.kt` |
| D3 | Không định dạng | Toolbar: **B** `**`, *I* `*`, tiêu đề `## `, bullet `- `, việc `- [ ] `, code `` ` `` | `Markdown.kt wrap/linePrefix`, `MdBtn` |
| D4 | — | Chế độ Xem render markdown; chạm ô ☑ đảo `[ ]`↔`[x]` trong văn bản, gạch ngang khi xong | `Markdown.kt Markdown()` |
| D5 | Lưu | Văn bản lưu nguyên dạng markdown (`item.text`), không đổi schema | `onSave(item.copy(text=…))` |

Cú pháp hỗ trợ: `# ## ###` · `**đậm**` · `*nghiêng*` · `` `code` `` · `- `/`* ` · `- [ ]`/`- [x]` · `> `.
Chưa hỗ trợ (ponytail): bảng, link, khối ``` , danh sách số. Thêm khi cần.

## v2 (21:41, Founder "chưa hài lòng" với v1)
| ID | v1 | v2 | Lý do |
|---|---|---|---|
| V1 | Thanh trên có chữ "Chi tiết" + nút tròn đen ✓ | Bỏ tiêu đề thừa; nút **Lưu** dạng pill; xoá icon xám trung tính (bỏ đỏ chói); icon mắt/bút đổi Sửa↔Xem | bỏ nhãn dư, 1 CTA chính (`primary-action`) |
| V2 | Ngày/nhắc là chữ trần | Chip bo tròn: xám (giờ), xanh nhạt (nhắc) | thứ bậc, đọc nhanh |
| V3 | Tiêu đề 24sp | 28sp Bold | tiêu đề là trọng tâm |
| V4 | Soạn trong Card nổi + chip Sửa/Xem + toolbar trong thẻ | Không khung: nội dung trực tiếp trên nền, đường kẻ mảnh, chữ 16/26sp, vùng gõ ≥320dp | giống Apple Notes, bớt khung lồng khung |
| V5 | Toolbar nằm giữa trang | Toolbar dính đáy, đẩy lên trên bàn phím (`imePadding`), ẩn ở chế độ Xem | ngón cái với tới; không chiếm chỗ đọc |
| V6 | Nút Thêm ảnh viền to | `AssistChip` bo tròn nhỏ, đặt sau Tag | gọn |

Ảnh v2: [Sửa](../assets/detail_v2_edit_20-10-2026.png) · [Xem](../assets/detail_v2_view_20-10-2026.png). Emulator: bố cục + toggle Sửa/Xem pass; toolbar đáy hiện đúng khi không có bàn phím; chưa kiểm khi bàn phím mở (toolbar đẩy lên) và trên Redmi.

v2 polish (21:44): nút toolbar đáy phẳng (`material3.IconButton`, hết nền xám tròn); Tag + nút ảnh gom `Column` gap 6dp. Ảnh: [Sửa](../assets/detail_v2_polish_20-10-2026.png). Đã cài Redmi `5d6c1a88` (Success); chờ Founder xem.

## States
| State | Mobile | Quyết định |
|---|---|---|
| Sửa | ● | toolbar + ô gõ, placeholder "Viết ghi chú… hỗ trợ Markdown" |
| Xem, có nội dung | ● | render markdown, ☑ chạm được |
| Xem, trống | ● | "Chưa có nội dung" |
| Không chọn chữ + bấm B | ◐ | chèn cặp `****`, con trỏ giữa |
| Có nhắc / không nhắc | ● | chỉ hiện nhãn giờ nhắc khi có |

## Verification
| Check | Status |
|---|---|
| Build `:app:assembleDebug` | verified 20-10 |
| Emulator Sửa: bố cục, toolbar chèn tiền tố/ký hiệu ([ảnh](../assets/detail_after_edit_20-10-2026.png)) | verified |
| Emulator Xem: ☑ + gạch ngang, bullet, nghiêng; chạm ☑ đảo văn bản ([ảnh](../assets/detail_after_md_view_20-10-2026.png)) | verified |
| Back → danh sách, không lưu, không thoát app | verified |
| Render `##`, `**đậm**`, `` `code` `` ở Xem | chưa kiểm bằng mắt (code đã viết) |
| Lưu note markdown rồi mở lại | chưa kiểm |
| Redmi | chờ Founder |
| Thẻ danh sách + `ViewScreen` (Recap) | vẫn hiện chữ thô, chưa render markdown |
| Test `adb input text` | không ổn định với Gboard (lỗi công cụ, không phải app) |

## v3 (20-10, Founder chọn: Autosave, Xoá vào ⋮, chip gợi ý; chạy 6 bước impeccable)
Thay thế v2 ở các điểm: **không còn chế độ Xem, không còn nút Lưu**. Soạn = xem trong một ô (`mdTransform`, giữ nguyên độ dài chữ nên con trỏ không lệch).

| # | Bước | Thay đổi | Ảnh |
|---|---|---|---|
| 1 | harden | autosave: debounce 600ms + ON_STOP + onDispose; nháp giữ qua xoay màn (`rememberSaveable`) | [s1](../assets/detail_v3_s1_harden_20-10-2026.png) |
| 2 | layout | thanh trên chỉ Back + ⋮ (Xoá trong ⋮) | [s2](../assets/detail_v3_s2_layout_20-10-2026.png) |
| 3 | adapt | chạm ≥48dp, khoảng cách ≥8dp, `heading()`, contentDescription, `Role.Button` | [s3](../assets/detail_v3_s3_adapt_20-10-2026.png) |
| 4 | distill | chip tag pastel + hàng "Tag +"; ảnh tối đa 240dp, chạm để phóng; thao tác ảnh vào ⋮ | [s4](../assets/detail_v3_s4_distill_20-10-2026.png) |
| 5 | clarify | tiêu đề sửa tại chỗ (`BasicTextField`); chip nhắc chạm được (Đổi giờ / Huỷ nhắc); chip "Gợi ý: <thư mục>"; chip "Đặt nhắc" khi chưa có | [s5](../assets/detail_v3_s5_clarify_20-10-2026.png) |
| 6 | polish | `Tok.Ink2` #6B6B78→#5C5C69; `blueInk()` (#0050B3 sáng / #6FB1FF tối) cho chữ trên nền xanh 12%; kiểm font 1.3× + theme tối | [1.3×](../assets/detail_v3_s6_polish_20-10-2026.png) · [tối](../assets/detail_v3_s6_dark_20-10-2026.png) |

Tương phản (tính tay, sáng): chip nhắc #0050B3 trên xanh 12%/#F2F2F7 ≈ 5.8:1; chip giờ #5C5C69 trên #E5E5EA ≈ 5.2:1. Tối: #6FB1FF chưa đo số, chỉ xem ảnh.

### Verification v3 (emulator-5554)
| Check | Status |
|---|---|
| Build `assembleDebug` sau mỗi bước | verified |
| Autosave, ⋮ → Xoá, Back về danh sách | verified (s1, s2) |
| Chip nhắc → menu; Huỷ nhắc → chip "Đặt nhắc" | verified |
| Font 1.3× thẻ + Chi tiết không vỡ | verified |
| Theme tối: danh sách + Chi tiết (chip nhắc đọc được) | verified bằng mắt |
| Đổi giờ nhắc end-to-end; Đặt nhắc → chọn ngày/giờ → lưu | **chưa kiểm** (emulator chậm, 2 lần lỗi) |
| Sửa tiêu đề → autosave | **chưa kiểm** |
| Chip "Gợi ý: <thư mục>" | **chưa kiểm** (cần thư mục trùng tên tag) |
| Ô ☑ trong dòng: không có `Role.Checkbox`, vùng chạm dọc ≈28dp | giới hạn đã biết |
| Thẻ danh sách + `ViewScreen` | vẫn chữ markdown thô; composable `Markdown()` không còn dùng ở Chi tiết |
| Splash ở theme tối | vẫn nền sáng |
| Redmi | **nhận APK bước polish ngoài yêu cầu** (`installDebug` cài cả 2 máy); chưa xem trên máy thật |
