# Phase 11 — Popup + thông báo nhắc khi note đến giờ

## Quyết định
- Tái dùng `Remind.kt` (đã có báo thức + thông báo). Thêm `RemindActivity` (hộp thoại: Xong / Hoãn 1 giờ / Đóng), mở bằng full-screen intent + `PendingIntent.send()` (app có quyền overlay ⇒ mở được khi đang dùng máy).
- `setAlarmClock` thay `setAndAllowWhileIdle`: đúng giờ, không cần quyền exact-alarm; đổi lại có icon đồng hồ trên status bar.
- Bỏ qua nhắc nếu note đã xoá / done / ended.
- `RemindBootReceiver`: đặt lại báo thức sau khởi động lại máy.
- `cancelRemind` huỷ luôn thông báo đang hiện.

## Checklist
- [x] Popup `RemindActivity` + manifest
- [x] Thông báo heads-up + full-screen intent
- [x] Đặt lại báo thức sau boot
- [x] Build xanh, cài lên máy
- [x] Thử thật trên máy: Founder xác nhận có popup + thông báo

## Giới hạn
- HyperOS có thể chặn mở popup từ nền: cần bật "Hiển thị trên màn hình khoá"/"Cửa sổ bật lên khi chạy nền" cho Recap; khi bị chặn vẫn còn thông báo.
- USE_FULL_SCREEN_INTENT trên Android 14+ có thể phải cấp tay trong Cài đặt.

## Bổ sung: nhắc lỡ khi tắt máy
- Máy tắt thì không thể nổ; khi bật lại `RemindBootReceiver` bắn ngay các note quá hạn <24h chưa nổ lần nào (`remindCount = 0`), ≥24h bỏ qua để không dội thông báo cũ.
- [x] Founder xác nhận: có popup sau khi tắt/bật máy.

## Bug: quẹt trái xoá card → văng app (đã sửa)
- Nguyên nhân: trigger `item_touch` (phase 10.6) tự `UPDATE Item SET updatedAt = giây*1000`; cùng giây ⇒ `updatedAt` không đổi ⇒ tự kích lại ⇒ "too many levels of trigger recursion" (SQLite) khi `softDelete`.
- Sửa: `updatedAt = MAX(OLD.updatedAt + 1, now)` ⇒ luôn khác OLD, không tự kích; Room v6→v7 drop + tạo lại trigger.
- Kiểm trên máy: quẹt trái → "Đã xoá · giữ 30 ngày", 0 crash; Hoàn tác OK.
- Lưu ý: ảnh hưởng mọi `UPDATE Item` (không riêng xoá) — các thao tác Xong/Hoãn trước đó cũng dễ dính.

## Vòng U1–U3 + E13 (20-10-2026, emulator)
- [x] E13 quẹt phải thẻ Xong → Mở lại: pass (Hoàn thành 1→0, Chờ nhắc 1→2)
- [x] U1/B10 sheet: `Theme.Recap.Sheet` trong suốt, hết đen status/nav bar
- [x] U2/E9 chạm thẻ = mở chi tiết (`tapOpens` mặc định true)
- [x] U3/B12 chú thích quẹt rõ hơn + menu giữ có "Hoàn thành/Mở lại"
- [x] Back ở màn Chi tiết thoát app → thêm `BackHandler` trong `DetailScreen` (`KhoScreen.kt`); emulator: Back về danh sách ([ảnh](../assets/u_after_back_20-10-2026.png))
- [ ] Founder xem U1 trên Redmi
- Ảnh: `documents/assets/u_{before,after}_*_20-10-2026.png`

## Quẹt phải = hoàn thành
- `TimelineRow` (`ui/Timeline.kt`): `SwipeToDismissBox` bật StartToEnd khi `!readOnly && !noStatus`; nền xanh + dấu ✓. Quẹt phải thẻ chưa xong → `done` (huỷ nhắc); thẻ đã `done` → mở lại (`open`). Thẻ bật về chỗ cũ, không bị xoá khỏi danh sách.
- Kiểm trên máy: open → done (thẻ 02), done → open (thẻ 03), 0 crash.
- Lưu ý: bắt đầu quẹt sát mép trái màn hình sẽ kích cử chỉ Back của hệ thống → bắt đầu cách mép ~1 cm.

## Nhấn giữ mic → ghi âm, nhắc thì phát lại
- Quyết định: mic không còn bấm-bật/bấm-tắt chép chữ trực tiếp; **nhấn giữ = ghi, nhả = dừng**. File WAV 16kHz mono lưu `files/voice_<ms>.wav`, cột mới `Item.audioPath` (Room v7→v8). Nhả tay vẫn chép chữ 1 lần (Whisper) để parse giờ + AI như cũ; thiếu model ⇒ vẫn giữ ghi âm, chữ để trống. Chạm <0.6s = bỏ. Cắt ở 60s.
- Sheet: chip "Nghe lại" / "Xoá ghi âm"; lưu được note chỉ có ghi âm (+ chọn giờ nhắc tay).
- Tới giờ: `RemindActivity` tự phát (luồng `USAGE_ALARM`) + nút "▶ Nghe lại"; thông báo nếu text trống hiện "Ghi âm nhắc nhở". Xoá vĩnh viễn note ⇒ xoá luôn file ghi âm; thoát sheet chưa lưu ⇒ xoá file mồ côi.
- Checklist:
  - [x] Db v8 + `audioPath`
  - [x] Nhấn giữ ghi WAV + chip nghe lại/xoá
  - [x] Phát lại ở popup nhắc
  - [x] Build xanh, cài lên máy
  - [x] UI: voice nối text bằng \n · nút "Xoá hết chữ" · không auto-focus · ô chữ readonly + icon bút/✓ để sửa · hàng [AI phân tích][+ ảnh][Lưu note] · AI chỉ chạy khi bấm icon AI, nút "Dùng bản hiểu để sửa" đưa kết quả vào ô chữ · bỏ chip giờ nhanh + nút Dán + dòng gợi ý (còn "Ngày giờ", góc trên-trái cùng hàng icon bút) · nút lưu = icon ✓ · mở sheet = tự ghi âm, chạm mic để dừng → tự chép chữ + AI phân tích · mic luôn xanh, 4 vòng nhấp nhô + "Đang nghe…"
- [ ] Thử thật trên máy: giữ mic → lưu → tới giờ nghe lại (adb tap bị chặn, Founder thử tay)
## Workflow ghi âm → chữ → AI (đề xuất, chưa code phần ✓ = đã có)

| # | Bước | Chi tiết | Trạng thái |
|---|---|---|---|
| 1 | Mở sheet → nghe | `startRec()` tự chạy; xin quyền micro nếu chưa có; mic xanh + vòng nhấp nhô | ✓ |
| 2a | Tắt thủ công | chạm mic lần 2 | ✓ |
| 2b | Tắt tự động (VAD) | đo RMS mỗi 100ms; đã có tiếng nói + im > 3s → `stopRec()` | ○ |
| 2c | Không ai nói | mở sheet > 8s vẫn im → dừng + bỏ (không tạo file, không chạy Whisper) | ○ |
| 2d | Lọc nội dung thoại | cắt im lặng đầu/cuối trước khi đưa Whisper; đoạn không có tiếng nói → bỏ; lọc câu ảo của Whisper khi im ("Cảm ơn đã xem…") | ○ |
| 3 | Show text | "Đang chép lời…" → lời hiện trong ô chữ (readonly, bút để sửa) | ✓ |
| 4 | Phân tích | có text → `understand()`: Hiểu → Giờ nhắc → Loại + tag | ✓ |
| 5 | Thoại thêm | chạm mic lần nữa → đoạn mới nối đuôi WAV cũ, chỉ chép đoạn mới, text nối bằng `\n`, AI chạy lại trên toàn bộ text | ✓ (thủ công) |

**Ghép 2 đoạn voice thành 1: được, đã làm.** PCM các đoạn nối chung 1 buffer → 1 file WAV; nhắc giờ phát cả hai đoạn liền mạch. Trần 60s tổng.

**Góp ý thêm**
- VAD dùng ngưỡng RMS đơn giản, có núm chỉnh (môi trường ồn → ngưỡng cao hơn); đoạn im 3s để lại trong file là bỏ (cắt đuôi im) để nhắc giờ không phải nghe im lặng.
- Đã ấn dừng tự động → mic quay về trạng thái chờ; user chạm lại để nói tiếp (không tự nghe lại, tránh thu nhầm tiếng ồn).
- AI chạy lại sau mỗi lần text đổi (đã có `understand()` bỏ kết quả cũ nếu text đổi giữa chừng).
- App vào nền (`onPause`) → dừng ghi, tránh giữ micro.
- Hai đoạn liền nhau < 1s: chèn 0,3s im lặng giữa hai đoạn để nghe lại không dính chữ.

- Giới hạn: Drive backup chưa gồm file ghi âm (restore ⇒ mất tiếng, còn chữ); popup bị HyperOS chặn nền ⇒ chỉ thông báo, tiếng phát khi mở popup/bấm thông báo.

## Bổ sung 20-10 (chiều)
- [x] AI quyết định có cần nhắc không: `aiWhen` trả `When(remind, at)`; `remind=false` (ghi chép/việc đã qua/nhắc giờ lướt qua) ⇒ bỏ cả giờ do luật `parseWhen`
- [x] Icon ✕ trên pill "Sẽ nhắc lúc …" → `dismissed` (xoá giờ nhắc; chọn lại Ngày giờ thì bật lại); `save()` dùng chung `remindAt()`
- [x] Bottom sheet + bàn phím: cột cuộn được (`verticalScroll`) + tự cuộn xuống ô chữ/nút lưu khi bàn phím mở
- [ ] Thử thật trên máy: câu "ghi chép" không ra giờ nhắc · câu "mai 9h họp" ra giờ nhắc · ✕ xoá · sheet khi gõ phím
- [x] Whisper: beam 3 + bỏ audio_ctx đo 17s audio → 57s (quá chậm) ⇒ quay lại greedy + audio_ctx = len/320+150 (đệm 3s). Chờ đo lại (logcat tag Whisper); nếu vẫn chậm: giảm threads 6→4, cắt lặng, PhoWhisper tiny
- [x] VAD tối giản: RMS > `VOICE_RMS`(600) = có tiếng; nói rồi im 3s → tự `stopRec()`; 8s chưa nói → `cancelRec()` bỏ đoạn. Chưa làm: cắt lặng, lọc ảo giác Whisper, dừng khi `onPause`
- [x] Menu nổi: "Sao chép" → MainActivity `go=clip` (tab Clipboard, icon `ic_clipboard`); "Sắp xếp kho" → "Ghi chú" `go=note` (tab Note, icon `ic_note`); `MainActivity.onNewIntent` → `recreate()` để app đang mở vẫn nhảy đúng tab
- [x] Menu nổi kính mờ: blur nền 40px + phủ trắng 25%, ô trắng 80% viền sáng, nhãn kính tối + viền; animation bung từ nút giữa (lệch nhịp 25ms). Chờ xem trên máy (blur phụ thuộc HyperOS bật cross-window blur)
- [x] Chép lời nhanh: `SpeechRecognizer` (dịch vụ Google, vi-VN) đọc PCM của app qua pipe `EXTRA_AUDIO_SOURCE` (Android 13+) — chữ hiện live khi đang nói; Google cắt câu thì chốt + mở lượt mới; lỗi/không ra chữ/6s không phản hồi → Whisper dự phòng. Manifest `<queries>` gói Google. Chưa thử trên máy: nếu log tag `Voice` báo "SR lỗi" thì dịch vụ Google không nhận audio source
- [x] SpeechRecognizer: Google RecognitionService không có trên HyperOS CN (lỗi 10) → tắt nhánh SR, chỉ Whisper
- [x] Model: PhoWhisper-base (dongxiat, 148MB) thay whisper.bin; bản cũ giữ `whisper_small_q.bak`
- [x] jni.c: `max_tokens = len/1600+24`, `temperature_inc = 0` (chặn decoder lặp: 3s audio → 22s)
- [x] `aiClean`: DeepSeek lọc tạp âm/câu ma + sửa chính tả trước khi hiện chữ Whisper
- [x] Nút bút sửa nhỏ lại (28dp/icon 16dp); bấm → thành ✕ huỷ: khôi phục chữ trước khi sửa + tắt bàn phím
- [x] Hiện số giây đang nghe (m:ss, màu xanh) ngang bên phải nút mic
- [x] Bố cục: mic bên trái; bên phải = giờ nghe (khi ghi) hoặc 2 nút Nghe lại + Xoá ghi âm xếp dọc
- [x] Whisper 6→4 thread: decode 1115 → 5 ms/token; 8.18s audio → 549ms (trước 28–44s). Nguyên nhân: 6 thread dính 2 nhân nhỏ (cpu0-1) kéo từng bước decode
- [x] Nút AI (✦): có ghi âm → Whisper nghe lại cả file + aiClean → điền lại chữ → phân tích; không có ghi âm → chỉ phân tích
- [x] Voice nhiều clip: mỗi clip 1 dòng [chữ sửa được] [✕ xoá chữ+tiếng] [▶ phát lại] (VoiceActivity.clips)
- [x] Voice: ô clip cuộn (max 240dp); AI ④ tiêu đề+tóm tắt (aiBrief voice) hiện trong khung AI, lưu vào Item.title/summary → thẻ Timeline hiển thị (chưa thử trên máy)
- [x] Voice: mở Ghi chú nhanh KHÔNG tự ghi âm nữa (chạm mic mới nghe)
- [x] Voice: mic thu nhỏ (52dp, icon 24dp) khi không nghe; phóng to 88dp khi đang nghe
- [x] Voice: AI không tự phân tích; chỉ chạy khi bấm ✦ (chữ xong) hoặc lúc lưu; bỏ re-listen ghi đè chữ
- [x] Voice: dừng mic khi chưa nói (heard=false) → bỏ đoạn, báo 'Không nghe thấy gì'; Whisper chỉ ra tạp âm → không tạo clip rỗng
- [x] Voice: 'Xoá ghi âm' xoá luôn chữ của clip có tiếng (chữ gõ tay giữ)
- [x] Voice: nút 'Gộp' giữa 2 clip → nối chữ + nối tiếng thành 1 clip
- [x] Thẻ Timeline: icon loa phát audioPath (các voice đã gộp 1 file WAV lúc lưu: rewriteAudio nối PCM các clip)
- [x] Thẻ Timeline: loa toggle phát/dừng (icon Stop khi đang phát, tự về loa khi hết)
- [x] Chi tiết note (KhoScreen.DetailScreen): nút Thêm/Đổi/Bỏ ảnh (1 ảnh/note); ảnh cũ xoá khi Lưu
- [x] Chi tiết note redesign + nhập Markdown (`Markdown.kt`): Sửa/Xem, toolbar B/I/tiêu đề/list/việc/code, ☑ chạm đảo được; emulator pass ([doc](../05-designs/ui_detail_markdown_20-10-2026.md), [ảnh Sửa](../assets/detail_after_edit_20-10-2026.png), [ảnh Xem](../assets/detail_after_md_view_20-10-2026.png))
- [x] Chi tiết v2 (Founder chưa hài lòng v1): bỏ tiêu đề/khung thẻ, chip ngày-nhắc, nút Lưu pill, toolbar dính trên bàn phím, mắt/bút đổi Sửa↔Xem; emulator pass ([ảnh Sửa](../assets/detail_v2_edit_20-10-2026.png), [ảnh Xem](../assets/detail_v2_view_20-10-2026.png)); APK đã cài Redmi
- [ ] Founder thử Chi tiết v2 trên Redmi (kiểm toolbar khi bàn phím mở); thẻ danh sách/Recap chưa render markdown
- [x] Quick note sheet restyle giống thẻ Timeline (khung 24dp, bóng, đinh ghim, panel pastel)
- [x] Fix UI note nhanh: panel clip ≤150dp khi có bàn phím, ✕/▶ dời lên đầu clip, viền focus cam
- [x] Padding ô clip: Row 14/8/8/8dp, chữ +4dp dọc, ✕/▶ 28dp
- [x] Bỏ title "Ghi chú nhanh" và chữ "Mới" trên sheet
- [x] VoiceActivity windowSoftInputMode=adjustNothing (sheet sát bàn phím, hết khoảng hở)
- [x] Test máy thật: sheet sát bàn phím OK, viền cam, ✕ ở đầu clip, header gọn
- [x] Panel clip tự cuộn xuống cuối khi text đổi (thấy con trỏ khi gõ dài)
- [x] Mic tự tắt: sàn ồn thích nghi (floor ≤1500, ngưỡng max(600, floor×2,2)); nói xong im 2,5s → stop; không nói 8s → huỷ. Test máy thật OK
- [x] Cuộn trong khung xanh: padding đặt trước verticalScroll; ô clip không trôi
- [x] Sửa bất cập từ test người dùng thật (emulator Pixel 9 Pro, bằng chứng ảnh f2–g1): Whisper chống bịa (bỏ clip nếu <3 khung có tiếng hoặc <600ms; cắt lặng, chừa 1s đầu/cuối) · max_tokens len/1000+24 (khỏi mất đuôi) · preload model (dòng bị comment nuốt) · ✕ dời lên hàng mic (hết khoảng trống khi bàn phím) · Back/✕/scrim: lần 1 Toast "Chưa lưu", lần 2 mới đóng · ✓ luôn thấy (tự cuộn khi bàn phím/panel AI) · AI 4 việc chạy song song, lỗi hiện icon + gợi ý bấm ✦ thử lại · chạm dòng chữ = vào sửa · ▶ đổi Stop khi đang phát
- [x] parseWhen: ngày không giờ → mặc định theo buổi (sáng 08 / trưa 12 / chiều 15 / tối 19); hôm nay + buổi đã qua → +1 giờ (RemindTest có case)
- [x] Test thật: Gộp (2 clip → 1), ▶ từng clip (MediaPlayer 16kHz), ✓ lưu → Timeline có icon loa
- [x] parseWhen số bằng chữ ("tám giờ tối mai", "tám giờ rưỡi") + RemindTest xanh
- [ ] Còn lại (giới hạn đã biết): Whisper ~30s/câu ngắn, không có tiến trình · ▶ giữ USAGE_ALARM (cố ý, nghe được cả khi tắt chuông media) · chất lượng tiêu đề AI phụ thuộc DeepSeek · guard Whisper chưa thử bằng giọng thật trên emulator (không có whisper.bin) · ✎ gõ adb mất 2 ký tự đầu (lỗi công cụ test, không phải app)
- Quyết định: test chỉ trên emulator từ giờ (Zalo bong bóng máy thật chặn tap); xoá note test "Mua dao…" trên máy thật do Founder tự làm (nhắc 19:00 21/10)
