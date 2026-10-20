# QA — Test chức năng trên emulator Pixel 9 Pro + bất cập với người dùng thật

Status: running · Ngày: 20-10-2026 · Phân loại: Jev `08-qa` 0.97 (auto)

## Scope
| Workflow | Vai trò | Route | Môi trường |
|---|---|---|---|
| Mở app, 4 tab (Note · Recap · Clipboard · Cài đặt) | Founder | MainActivity | AVD Pixel_9_Pro, Android 15 (API 35), 4GB RAM, Play image |
| Ghi chú nhanh (FAB +) → gõ → AI → lưu | Founder | VoiceActivity | như trên, có mạng |
| Thẻ Timeline: quẹt phải = Xong | Founder | TimelineRow | như trên |
| Nhắc giờ (`scheduleRemind`) | Founder | Remind.kt | như trên |

Không test được trên emulator: nút nổi/chụp (cần overlay + accessibility), Whisper (thiếu `whisper.bin`), Drive (cần đăng nhập Google), ghi âm thật (không có mic), HyperOS.

## Test Executions
| Execution ID | Case ID | Status | Actor | Executed | Evidence |
|---|---|---|---|---|---|
| E1 | Cài APK + mở app | passed | Claude | 20-10 18:47 | splash → màn Note |
| E2 | Xin quyền thông báo (Android 13+) | passed | Claude | 18:48 | hộp thoại Allow |
| E3 | Chuyển 4 tab | passed | Claude | 18:49 | Note / Recap / Clipboard / Cài đặt hiện đúng |
| E4 | FAB → sheet ghi chú nhanh | passed (chậm) | Claude | 18:50 | sheet hiện, mic xanh |
| E5 | Gõ chữ + parse giờ ("mai9hhop") | passed | Claude | 18:54 | pill "Sẽ nhắc lúc 09:00 21/10" |
| E6 | Lưu → AI tóm tắt + tag | passed | Claude | 18:54 | thẻ 01 có tiêu đề, tóm tắt, 3 tag |
| E7 | Quẹt phải = Xong | passed | Claude | 18:54 | thẻ xanh, gạch tiêu đề, "Hoàn thành 1" |
| E8 | Lưu note CÓ giờ nhắc → đặt báo thức | **passed sau fix B1** (trước: failed 2/2) | Claude | 18:54 + 18:55; chạy lại sau fix 20-10 | trước: `SecurityException` tại `scheduleRemind` ⇒ văng. Sau: không văng, `dumpsys alarm` có báo thức |
| E9 | Chạm thẻ → mở chi tiết | **passed (theo thiết kế: tab Note chạm = không làm gì; giữ → menu → Chỉnh sửa → mở "Chi tiết")** | Claude | 20-10 20:49 | chạm 2 lần không đổi (`tapOpens=false` ở tab Note, `Timeline.kt:265`); giữ 1s → menu Chỉnh sửa/Xoá → màn "Chi tiết" (tag, nội dung). Chạm mở trực tiếp chỉ ở tab Recap/Clipboard (chưa test) |
| E10 | Quẹt trái xoá | passed | Claude | 20-10 20:54 | thẻ biến mất, snackbar "Đã xoá · giữ 30 ngày / Hoàn tác", "Chờ nhắc 2→1" |
| E11 | Tạo note "mai 9h hop" có giờ nhắc → không văng + báo thức | passed (một phần: chưa chờ tới giờ + popup) | Claude | 20-10 20:50 | pill "Sẽ nhắc lúc 09:00 21/10", lưu OK, không crash (logcat -b crash rỗng), `dumpsys alarm` có `RemindReceiver` origWhen=1792548000000 (21/10 09:00 +07) |
| E12 | B7: tag note mới không chứa hop/9h/mai | passed | Claude | 20-10 20:50 | thẻ mới "Họp 9h sáng mai" không có tag nào |
| E13 | Quẹt phải đã-xong → Mở lại | **passed sau fix** (`rememberUpdatedState` ở `SwipeCard`; mở lại đặt lại báo thức nếu `remindAt` còn tương lai, `dumpsys alarm` origWhen 21/10 09:00) — lần fail ban đầu: | Claude | 20-10 20:51–20:53; chạy lại 21:00 | 4/4 lần quẹt phải trên thẻ đã Hoàn thành: không mở lại; 2 lần bật menu giữ (Chỉnh sửa/Xoá) thay vì kéo. Quẹt phải lần đầu (open→done) OK. Nghi: SwipeToDismissBox kẹt state sau `confirmValueChange=false` (`Timeline.kt:239`) |

## Result
| Passed | Failed | Blocked | Risk |
|---|---|---|---|
| 8 | 0 | 3 | B1 đã fix + verify. B2 đã sửa code, **chưa verify** (emulator không có mic, phiên emulator đã đóng) |

## Bất cập (góc nhìn người dùng thật)
Mức: 🔴 mất dữ liệu/văng · 🟠 hỏng trải nghiệm · 🟡 gợn

| ID | Mức | Phát hiện | Bằng chứng / vị trí | Đề xuất ngắn nhất |
|---|---|---|---|---|
| B1 | 🔴 | `setAlarmClock` ném `SecurityException` không bắt ⇒ lưu note có giờ nhắc làm **văng app**; note đã ghi DB nhưng không có báo thức, sheet đóng như thể xong. Trên máy Founder (HyperOS) đã chạy được, emulator API 35 thì không ⇒ phụ thuộc ROM/quyền | `Remind.kt:112`, `VoiceActivity.kt:448` | bọc `runCatching`, lỗi ⇒ toast "Chưa đặt được nhắc" + mở `ACTION_REQUEST_SCHEDULE_EXACT_ALARM`; thêm `SCHEDULE_EXACT_ALARM` vào manifest |
| B2 | 🔴 | Vòng đọc mic `if (n <= 0) continue` không ngủ, không xử lý mã lỗi ⇒ mic bận/mất quyền/không có mic = quay vô hạn 100% CPU, **không bao giờ chạm nhánh "8s không nói → huỷ"**; mic xanh sáng mãi (quan sát >60s) | `VoiceActivity.kt:187` | `n < 0` ⇒ dừng + báo lỗi; `n == 0` ⇒ kiểm `quiet` trước khi `continue` |
| B3 | 🟠 | Phụ thuộc `whisper.bin` (148MB) cài tay vào `getExternalFilesDir`; máy mới/cài lại/xoá dữ liệu ⇒ giọng nói im lặng, chỉ có tin "thiếu whisper.bin" | emulator: thư mục files trống | màn Cài đặt: trạng thái model + nút tải/nhập file |
| B4 | 🟠 | Ghi chú nhanh gửi nguyên văn nội dung lên DeepSeek (cloud) khi lưu — ngược "không backend", người dùng không thấy công tắc/cảnh báo; offline ⇒ không rõ thẻ có tóm tắt hay không | thẻ 01 có AI ngay khi lưu | công tắc "AI đám mây" trong Cài đặt + nhãn thẻ "chưa phân tích" khi offline |
| B5 | 🟠 | Sheet có 100% frame giật (p90 550ms) trên emulator; gây ANR hệ thống khi gõ. Nguyên nhân khả nghi: animation vô hạn (mic, 4 vòng, orb) + blur trên GPU phần mềm. Máy thật chưa đo | `gfxinfo`: 24/24 janky, GPU p90 4950ms | đo `gfxinfo` trên Redmi; nếu giật ⇒ dừng animation khi không ghi/khi bàn phím mở |
| B6 | 🟡 | AI viết tóm tắt ngôi thứ ba: "Người dùng nhắc lịch họp…" — người dùng là chính mình; tiêu đề thừa chữ "Ghi chú" | thẻ 01 | sửa prompt `aiBrief`: câu mệnh lệnh/ngắn, bỏ "Người dùng", cấm tiền tố "Ghi chú" |
| B7 | 🟡 | Tag rác từ tách từ thô: `#hop #9h #mai` (giờ, từ thời gian thành tag) | thẻ 01 | lọc token thời gian + stopword khỏi tag |
| B8 | 🟡 | Nhãn "còn 14g" dễ đọc nhầm; slot "Tối 18:00 – 05:00" + thẻ "Bây giờ" thừa 2 lần | thẻ 01 | "còn 14 giờ"; bỏ badge trùng |
| B9 | 🟡 | Sau khi lưu, quả cầu xanh (orb) vẫn nằm đè lên đầu thẻ ở màn Note, đổi màu theo trạng thái (xanh lá khi done) — trông như lỗi hiển thị | ảnh t_list / t_done | xác nhận là ghim trang trí; nếu không ⇒ bỏ |
| B10 | 🟡 | Nền status/nav bar đen khi sheet mở, trắng khi ở main ⇒ nháy | ảnh sheet | `enableEdgeToEdge` thống nhất cho VoiceActivity |
| B11 | 🟡 | Tab ✦ tên "Recap" (app tên Recap) nhưng tab đầu tên "Note": người mới khó phân biệt Note vs Recap; Recap trống "Chưa có ghi nhận" dù đã có note | màn Recap | đổi nhãn hoặc thêm hint rỗng |
| B12 | 🟡 | Quẹt thẻ: không có gợi ý lần đầu (quẹt phải = Xong, trái = xoá, cách mép ~1cm) | phase_11 ghi chú | 1 dòng hướng dẫn lần đầu |

## Regression
| Workflow liên quan | Desktop | Mobile | Evidence |
|---|---|---|---|
| Quẹt xoá (trigger `item_touch`) | — | blocked | chưa chạy tới |
| Xong ↔ mở lại | — | ● (chiều Xong) | t_done |
| Parse giờ rule-based | — | ● | E5 |

## Follow-up
| Ref | Loại | Việc cần làm |
|---|---|---|
| B1 | bug | **fixed 20-10**: `runCatching` + toast trong `Remind.kt`, thêm `USE_EXACT_ALARM` vào manifest. Còn chạy lại E9–E11 |
| B2 | bug | **code fixed, chưa verify**: `n<0` ⇒ dừng + "Mic lỗi"; `n==0` ngủ 20ms, 8s chưa có tiếng ⇒ huỷ. Founder thử trên Redmi (mic thật + thử từ chối quyền mic) |
| B8, B12 | UI | fixed + verified emulator (xem `05-designs/ui_restyle_unstable_20-10-2026.md`) |
| B6 | bug | **fixed 20-10 (build + unit test xanh, chưa xem thẻ thật)**: prompt `aiBrief` (`Ai.kt`) cấm mở bằng "Người dùng"/tiền tố "Ghi chú", viết kiểu tự nhắc mình. Ghi note mới trên emulator để xác nhận |
| B7 | bug | **có vẻ đã hết**: tag nay do `aiAnalyze` sinh (≤3 slug), không còn tách từ thô. Xác nhận khi ghi note có giờ |
| B5 | UI | **đóng 20-10 (đo Redmi)**: mở sheet 591 frame, janky 0.17%, p90 14ms ⇒ chỉ giật trên emulator GPU mềm, không cần sửa |
| B10 | UI | **fixed + verified Redmi 20-10**: Theme.Translucent không vẽ nền thanh hệ thống ⇒ HyperOS tô đen; thêm `FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS` + `statusBarColor=TRANSPARENT` (`VoiceActivity.onCreate`), bỏ `statusBarsPadding` ở scrim ⇒ giờ/pin hiện đúng |
| E13 | UI | **fixed + verified emulator**; mở lại đặt lại báo thức khi `remindAt` còn tương lai |
| B3, B4 | backlog | `13-backlog` |
| — | QA | test máy thật cho phần emulator không phủ: nút nổi, chụp, Whisper, Drive, popup HyperOS |
| — | QA | **không dùng adb tap trên máy thật** (đã tap nhầm bong bóng Zalo, gửi nhầm ảnh) ⇒ chỉ emulator hoặc Founder thao tác |
