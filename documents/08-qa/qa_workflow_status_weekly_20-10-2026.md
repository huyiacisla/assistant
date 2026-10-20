# QA — Checklist status test workflow theo tuần + đề xuất UI/UX

Status: running · Ngày: 20-10-2026 (tuần ISO 43) · Phân loại: Jev `08-qa` 0.95 (auto, margin 0.91)

Nguồn số liệu: [qa_functional_emulator_pixel9pro](qa_functional_emulator_pixel9pro_20-10-2026.md) · [qa_device_redmi](qa_device_redmi_20-10-2026.md) · `04-phases/phase_*.md` · xếp hạng UX = `jev_rerank`.
Ký hiệu: ● pass · ◐ pass một phần / chưa verify · ○ chưa test · ✗ fail · — không áp dụng.

## 1. Số liệu tổng (tuần 43)
| Nguồn | Pass | Fail | Blocked/Note | Ghi chú |
|---|---|---|---|---|
| Emulator E1–E13 | 13 | 0 | 0 | E13 pass sau sửa 20-10 (bảng Result gốc ghi 8/0/3 = cũ) |
| Máy thật D1–D10 | 7 | 0 | 3 (D8 note, D9, D10 blocked) | UI chưa kiểm: máy khoá màn hình |
| Phase 10 Drive · 8 Note | 8/8 · 8/8 | — | — | checklist đã tick hết |
| Phase 11 Nhắc giờ | 52/55 | — | 3 chờ Founder | thử thật: mic, "mai 9h họp", giới hạn đã biết |
| Phase 12 Chụp 5 bước | 9/11 | — | 2 chờ Founder | e2e Facebook → bubble |
| Phase 9 Modular | 0/39 | — | — | làm dần, song song |
| Phase 1–7 | theo roadmap: 1–2 `[~]`, 3–7 `[ ]` | | | file dùng bảng, không đếm tự động |

## 2. Ma trận workflow × tuần
Cột = nguồn test; hàng = chức năng con.
| Chức năng con | Emulator | Máy thật (adb) | Founder tay | Tuần 43 | Việc tiếp |
|---|---|---|---|---|---|
| Mở app · 4 tab | ● E1,E3 | ◐ D1 | ○ | ● | — |
| Quyền (mic · overlay · alarm · thông báo) | ● E2 | ● D2–D5 | — | ● | — |
| Ghi chú nhanh (FAB → gõ → lưu) | ● E4–E6 | ○ | ○ | ◐ | thử trên Redmi |
| AI tóm tắt + tag | ● E6,E12 | ○ | ○ | ◐ | xem thẻ thật (B6, B7) |
| Parse giờ nhắc | ● E5 | — | ○ | ● | — |
| Đặt báo thức nhắc | ● E8,E11 (sau fix B1) | ● D6 | ○ chờ tới giờ + popup | ◐ | chờ tới 21/10 19:00 |
| Quẹt phải = Xong | ● E7 | ○ | ○ | ● | — |
| Quẹt phải thẻ đã Xong = Mở lại | ● E13 (sau sửa 20-10) | ○ | ○ | ● | — |
| Quẹt trái xoá + hoàn tác 30 ngày | ● E10 | ○ | ○ | ● | — |
| Chạm thẻ → chi tiết | ● E9 (đã sửa: chạm = mở) | ○ | ○ | ● | — |
| Chi tiết v3: autosave, ⋮, markdown 1 ô, chip nhắc/gợi ý | ◐ (6 bước; chưa kiểm Đổi giờ/Đặt nhắc/tiêu đề) | ○ | ○ | ◐ | Founder thử Redmi (đã nhận APK polish) |
| Mic + Whisper | — (thiếu bin) | ○ | ○ | ○ | Founder ghi âm thật (B2, B3) |
| Nút nổi → chụp → bong bóng Recap | — | ○ | ○ | ○ | Founder: Facebook e2e |
| Clipboard · Share sheet | ○ | ○ | ○ | ○ | chưa có case |
| Recap (tab ✦) | ◐ (trống) | ○ | ○ | ○ | cần dữ liệu + test |
| Drive sao lưu/khôi phục | — | ○ | ○ | ◐ | phase 10 đã tick; chưa có case QA |
| Hiệu năng sheet (B5) | ✗ 24/24 janky | ○ D10 blocked | — | ○ | đo `gfxinfo` sau mở khoá |
| Logcat crash | ● | ● D7 | — | ● | — |

## 3. Checklist tuần 43 (tick khi xong)
**Claude (không đụng máy thật)**
- [x] Tổng hợp số liệu QA + phase
- [x] Xếp hạng UX bằng Jev
- [x] Sửa E13 (quẹt phải mở lại) → emulator: Hoàn thành 1→0, Chờ nhắc 1→2 ([trước](../assets/u_before_e13_20-10-2026.png) · [sau](../assets/u_after_e13_20-10-2026.png)). Lần "fail" cũ: APK cũ + quẹt bắt đầu x<150 dính cử chỉ Back hệ thống (thoát app) — test quẹt phải bắt đầu x≥300
- [x] U1 B10 theme sheet trong suốt · U2 E9 chạm = mở chi tiết · U3 B12 gợi ý + menu "Hoàn thành" (ảnh trước/sau trong `documents/assets/u_*_20-10-2026.png`); Redmi chờ Founder
- [x] Back ở màn Chi tiết thoát hẳn app → đã thêm `BackHandler`, emulator pass
- [x] Chi tiết redesign + markdown (Sửa/Xem, toolbar, ☑ chạm) → emulator pass ([doc](../05-designs/ui_detail_markdown_20-10-2026.md)); chưa kiểm: render `##`/`**`, Lưu round trip, Redmi
- [x] Chi tiết v3 (6 bước impeccable: harden→polish), ảnh `detail_v3_s*` ([doc](../05-designs/ui_detail_markdown_20-10-2026.md)); chưa kiểm: Đổi giờ/Đặt nhắc→lưu, sửa tiêu đề, chip Gợi ý, Redmi
- [ ] Ghi note mới trên emulator, xác nhận B6 (tóm tắt) + B7 (tag)
- [ ] Cập nhật bảng Result của qa_functional (8/0/3 → 12/1/0)

**Founder (máy thật; không dùng adb tap)**
- [ ] Mở khoá, mở Recap: nhãn "còn … giờ", chú thích quẹt, badge "Bây giờ", nav bar khi mở sheet (B10)
- [ ] Báo lại để chạy `gfxinfo` (B5/D10)
- [ ] Mic + Whisper: đọc câu, kiểm transcript; thử từ chối quyền mic (B2)
- [ ] Facebook → nút nổi → Chụp & Lưu → Mở Kho (phase 12)
- [ ] "mai 9h họp" ra giờ nhắc · "ghi chép" không ra · ✕ xoá (phase 11)
- [ ] 21/10 19:00: báo thức nổ + popup

## 4. UI/UX cần cải thiện (Jev rerank + 3 skill UI)
Jev query: "ảnh hưởng lớn nhất, dùng hằng ngày, rủi ro thấp, làm nhanh". Relevance = xác suất Jev (chưa đo trên dữ liệu thật).

| Hạng | ID | Vấn đề | Jev | Gợi ý (rule ui-ux-pro-max) | Ước lượng |
|---|---|---|---|---|---|
| 1 | U1 = B10 | Status/nav bar nháy đen–trắng khi mở sheet | 0.76 | `enableEdgeToEdge` thống nhất VoiceActivity; scrim đo trên nền thật (`modal-motion`, `elevation-consistent`) | nhỏ |
| 2 | U2 = E9 | Chạm thẻ tab Note không làm gì; phải giữ → menu | 0.75 | chạm = mở chi tiết, giữ = menu (`standard-gestures`, `no-precision-required`) | nhỏ |
| 3 | U3 = B12 | Quẹt không có gợi ý lần đầu | 0.68 | hint 1 dòng + nút Xong/Xoá hiện trong menu (`swipe-clarity`, `gesture-alternative`) | nhỏ |
| 4 | U4 = B5 | Sheet giật frame | 0.62 | dừng animation vô hạn khi không ghi / bàn phím mở; chỉ animate transform/opacity (`transform-performance`, `excessive-motion`, `reduced-motion`) | vừa |
| 5 | U5 = B11 | "Note" vs "Recap" khó phân biệt; Recap trống | 0.62 | đổi nhãn + empty state có hành động (`nav-label-icon`, `empty-states`) | nhỏ |
| 6 | U6 = B9 | Orb đè đầu thẻ, đổi màu | 0.58 | xác nhận trang trí; nếu không → bỏ (`motion-meaning`, `color-not-only`) | nhỏ |
| 7 | U7 = E13 | Không mở lại thẻ Xong | 0.44 | là bug, không phải UX: reset state SwipeToDismissBox | vừa |
| 8 | U8 = B3 | Thiếu whisper.bin → im lặng | 0.38 | trạng thái model trong Cài đặt (`error-recovery`) | vừa |
| 9 | U9 = B2 | Mic quay vô hạn | 0.29 | đã sửa code; Founder verify | — |
| 10 | U10 = B4 | Không công tắc AI đám mây | 0.27 | công tắc + nhãn "chưa phân tích" (`offline-support`) | vừa |

Lưu ý: hạng 7–10 điểm thấp vì query thiên về "nhanh/ít rủi ro", không phải vì ít quan trọng — E13 là fail thật, B2/B4 chạm dữ liệu.
Soát chung theo checklist ui-ux-pro-max: touch target ≥48dp (nút ✕, icon thẻ) · tương phản ≥4.5:1 sáng/tối · khoảng cách ≥8dp · không emoji làm icon cấu trúc (B8 dùng 🔴🟠🟡 chỉ trong doc, OK).
`/impeccable` và `/web-design-guidelines`: chưa chạy audit thật (cần ảnh chụp màn hình máy thật; web-design-guidelines không có trong danh sách skill phiên này). Chạy sau khi Founder mở khoá.

## 5. Tuần kế (tuần 44) — trigger kiểm lại
| Việc | Điều kiện đóng |
|---|---|
| Lặp mục 2 cho các ô ○ | mỗi hàng ≥ 1 cột ● |
| Làm U1–U3 (cùng 1 vòng, đều nhỏ) | build + emulator ảnh trước/sau + Founder xem |
| Đo lại số liệu | cập nhật mục 1; Jev `rerank` lại sau khi đóng hạng 1–3 |
