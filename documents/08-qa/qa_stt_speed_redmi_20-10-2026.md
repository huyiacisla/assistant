# STT nhanh hơn — đo trên Redmi Turbo 4 Pro (thật)

Status: done · Ngày: 20-10-2026 · Phân loại: `08-qa` (cùng nhóm QA 20-10)

## Cách đo
TTS tiếng Việt (`say -v Linh`) → WAV 16kHz → `adb am broadcast … SttBench` (debug-only) → `Whisper.load/transcribe` trên máy thật → logcat `SttBench`. Không đụng mic/UI.

## Kết quả (run = ms, RTF = run / audio)
| Model | Kích thước | 6.0s | 2.5s | 11.7s | RTF | Ghi chú |
|---|---|---|---|---|---|---|
| small fp16 (cũ) | 487MB | 2500 | lặp 4× | — | 0.42 | chậm + ảo giác lặp |
| small q | 190MB | 3900 | — | — | 0.65 | không nhanh hơn |
| **PhoWhisper-base (mới)** | 148MB | **676** | 1081 (lần đầu) | **790–1464** | **0.07–0.12** | chọn |

Tăng tốc ≈ 3.7× (clip 6s). Clip 11.7s vẫn <1.5s ⇒ không cần chunk/stream.

## Thay đổi
- Máy: `whisper.bin` ← `whisper_base.bak`; bản cũ giữ `whisper_small_fp16.bak` (cùng thư mục `files/`).
- Code: bỏ log tạm `wlog` / `whisper_print_timings` ở `cpp/jni.c`. Tham số giải mã giữ nguyên (greedy, `audio_ctx`, `max_tokens`, 4 luồng — 6 luồng kẹt lõi bé).
- AI: `aiClean` (DeepSeek) đã nằm trong `quicknote.activity.kt::whisper()`, chạy sau Whisper, lỗi mạng ⇒ giữ chữ gốc. Không thêm độ trễ mới.

## Độ chính xác (base, TTS)
Sai nhẹ: "họp→học", "bé→bờ", "hai mươi lăm→hai mươi năm" ⇒ `aiClean` sửa đồng âm theo ngữ cảnh. Chưa đo bằng giọng thật.

## Mic thật Redmi (loa Mac → mic máy, `SttBench --ez open true` mở sheet + tự ghi, chỉ bản debug)
- Câu Việt thuần: 9,1s → Whisper 0,58s → `aiClean` ~1s → "…nhớ mang theo laptop." (sửa "lá tốt"→laptop). Đạt.
- Câu lẫn từ Anh (Linh đọc từ Anh theo âm Việt = test sai) → bỏ. Test đúng: ghép giọng Linh + Samantha (`scratchpad/mix.py`, m1–m3):
| Model | m2 "…client…invoice…Google Calendar…Jira" | RTF |
|---|---|---|
| base (PhoWhisper) | "quay ẩn…in võ…bốc mi tìm…iraq" ✗ | 0,1 |
| small_q (đa ngữ) | gần đúng hoàn toàn (email, client, invoice, Google Calendar, JIRA) ✓; m3 bị cắt câu | 0,4–0,45 |
⇒ base nhanh nhưng **kém từ Anh**; small_q đúng từ Anh nhưng chậm ~4–5s/10s. `aiClean` không cứu được khi âm đã sai hẳn.
- DeepSeek **không có** STT (chỉ text). Jev = phân loại, không phải STT.
- Lưu ý bench: `Whisper.load` bỏ qua `--es model` nếu đã nạp model khác trong process ⇒ `force-stop` trước mỗi lần.

## Bench sherpa-onnx Zipformer-vi (Redmi, CPU 4 luồng, `SttBench --es zip <thư mục>`)
Nguồn: HF `csukuangfj/sherpa-onnx-zipformer-vi-int8-2025-04-20` (zip77: encoder int8 71MB + decoder 5MB + joiner 1MB ≈ 77MB, ~70k giờ dữ liệu) và `csukuangfj2/sherpa-onnx-zipformer-vi-30M-int8-2026-02-09` (zip30 ≈ 34MB). Runtime: AAR `sherpa-onnx-1.13.8` (arm64: onnxruntime 22MB + jni ~9MB), thêm bằng `debugImplementation` ⇒ chỉ bản debug. Đây là bản **offline** (không streaming); repo chính thức không có Zipformer-vi streaming (chỉ bản đa ngữ ar/en/id/ja/ru/th/vi/zh 340MB, chưa thử).

| Model | Cỡ | Load | Run (clip 6s / 11,7s) | RTF | Việt thuần (a, c) | Lẫn Anh (m1–m3) |
|---|---|---|---|---|---|---|
| base PhoWhisper | 148MB | 0,4s | 0,68s / 0,8–1,5s | 0,07–0,12 | đúng, sai đồng âm nhẹ | ✗ nát (spurs…iraq) |
| small_q | 190MB | 0,4s | 4,2–4,9s (10–11s) | 0,4–0,45 | — | ✓ m1/m2 gần đúng, m3 bị cắt vụn |
| **zip77** | 77MB | 2–3,5s | **0,21s / 0,30s** | **0,025** | ✓ đúng hết (c: đủ số, "cho bớ" → aiClean sửa "bé") | ◐ phiên âm theo tiếng Việt |
| zip30 | 34MB | 2,2–2,7s | 0,17s / 0,31s | 0,03 | ✓ ("anh Năm" thay "Nam") | ◐ ngang zip77, m1/m2 nhỉnh hơn |

Từ Anh (chữ thô → sau `aiClean`):
- m1: zip77 "SREAT LANING…REALT…DLOI…STAING" → "sprint planning…review requirement…" (✗ requirement); zip30 "SPIT PLANTING…TIM BUGUND…PROBIS…DEPOINT…STATION" → "sprint planning, team backend, review progress, deploy, staging" (◐); small_q → "Sprint Planning…Team Backend…review pull request…deploy…staging" ✓.
- m2: zip77 "CLIANS…INBOIS…BOS MING…CALLANDER…OFE SHAD…GER" → "clients…invoice…bỏ meeting…Calendar…off sheet…Git" ◐; zip30 "CLIENT…BOOK MEENING…CALL ONDER" → "client…book meeting…Calendar under" ◐; small_q ✓ (Jira đúng).
- m3: zip77 "PHÍCHAGEN…PUS" → "check lại API rồi push lên kết hợp" ✗; zip30 → "FIX BUG…APK…PUSH CODE LÊN GIT" (viết hoa hết, thiếu GitHub) ◐; small_q bị cắt vụn.
- `aiClean` có thể **bịa** khi chữ thô quá lệch (zip77 m3 "check lại API"): chỉ tin khi chữ thô gần đúng.

Kết luận:
- Việt thuần: Zipformer **nhanh 4–5× base, 15–20× small_q**, chính xác ≥ base. Thắng rõ.
- Từ Anh: Zipformer chỉ phiên âm, không đúng chính tả; small_q vẫn tốt nhất nhưng chậm. Bản 30M ≈ bản 77M ở câu lẫn Anh ⇒ không cần 77M.
- Lưu ý: test từ Anh là TTS Samantha ghép Linh (không phải người Việt nói thật) ⇒ kết luận về từ Anh chưa chắc; cần giọng Founder.
- Đề xuất: Zipformer-30M (34MB) làm mặc định (0,3s), giữ small_q làm nút "nghe lại kỹ" cho câu lẫn Anh. Chi phí: +~31MB `.so` cho APK release + Kotlin glue; chưa tích hợp vào `main` (đang chờ Founder đồng ý).
- Bench: nhánh `--es zip` trong `SttBench` (debug).

## Tích hợp vào main (Founder duyệt 20-10-2026)
- `app/libs/sherpa-onnx-1.13.8.aar` (`implementation`, arm64) + `feature/voice/zipformer.engine.kt` (`Zipformer.ready/load/transcribe`, hạ chữ thường vì model xuất CHỮ HOA).
- `VoiceActivity`: `chep(seg, careful)` — mặc định Zipformer-30M (`files/zip30/`), thiếu model → Whisper; nút **Nghe kỹ** (cạnh Nghe lại/Xoá ghi âm) chép lại clip có tiếng cuối bằng Whisper small q (`files/whisper_q.bin`, thiếu → `whisper.bin`) rồi `aiClean`, thay chữ clip đó. Preload Zipformer ở `onCreate`.
- Máy Redmi: `whisper_small_q.bak` → `whisper_q.bin`; xoá thư mục thử `zip77`. Model **không đóng trong APK** (adb push như Whisper trước đó).
- Debug-only: `SttBench --ez open true --es wav <tên.wav>` nạp WAV thẳng vào luồng `VoiceActivity` (không cần mic).
- Kiểm trên Redmi (WAV c.wav, 11,7s): Zipformer → `aiClean` đủ câu đúng, "cho bớ" → "cho bé" ✓; chạm Nghe kỹ → Whisper small 3,3s → "…cho bé, tối về nhớ đóng tiền điện trước ngày 25." ✓ (số dạng chữ số). Không crash.
- Giới hạn: mic thật qua loa Mac không đủ to (chỉ bắt tiếng động) → chưa test mic acoustic sau tích hợp; từ Anh vẫn cần "Nghe kỹ". Nghe kỹ thay chữ clip ⇒ sửa tay trước đó của clip đó mất.

## Kiểm sau tích hợp (Redmi, WAV m1–m3 → `VoiceActivity`, 20-10-2026)
Debug hook mới: `SttBench --ez open true --es wav m1.wav --ez careful true` = ghi → Zipformer → `aiClean` → tự chạy "Nghe kỹ" (`input tap` trên máy ngừng nhận chạm, nên không bấm tay).

| Clip | Mặc định (Zipformer-30M + aiClean) | Sau Nghe kỹ (small q + aiClean) |
|---|---|---|
| m1 | "họp … team planting … review proposal … deploy lên staging" ◐ (aiClean đổi kết quả giữa các lần chạy) | "Sprint Planning với team Backend … review pull request … deploy lên staging trước 5 giờ" ✓ (4,0s) |
| m2 | "…về Ann Boys … mời David Traders trên Trello" ✗ bịa | "…về invoice … Google Calendar … update status trên JIRA" ✓ (4,1s) |
| m3 | "fix bug … app drone, build lại APK … push code lên Git" ◐ | "fix bug login … app Android … push code lên GitHub" ✓ (3,6s) |

⇒ Pipeline đúng thiết kế: mặc định nhanh (<1s), câu lẫn Anh bấm Nghe kỹ ≈ 4s → đúng từ Anh. Không crash, không lỗi logcat. `aiClean` có thể bịa khi chữ thô lệch (m2 "Ann Boys/Trello").

## UI chỉnh (20-10-2026)
- Đinh ghim xanh của thẻ note → icon mic; mic này **chính là nút ghi âm** (bỏ mic ngoài).
- 3 nút Nghe lại / Nghe kỹ / Xoá ghi âm: bỏ chữ, nút tròn icon **rời nhau** (không group) ngang hàng mic — [Nghe lại][Nghe kỹ] trái, [Xoá ghi âm] phải; chỉ hiện khi có tiếng và không đang nghe; có `contentDescription`. Icon Nghe kỹ = `Hearing`.
- Chip "Xoá hết chữ" chuyển lên hàng icon sửa (ngay trước nút bút, cùng hàng với "Ngày giờ").
- Bỏ dòng giờ (HH:mm) trên hàng icon sửa. Đã chụp Redmi xác nhận (`m2.wav`).

## Checklist
- [x] Đổi model, bench 3 clip trên Redmi
- [x] Gỡ log tạm, build debug xanh, cài Redmi + emulator
- [x] `aiClean` xác nhận có trong luồng
- [x] Bench sherpa-onnx Zipformer-vi (77M + 30M) vs base vs small_q trên Redmi
- [x] Tích hợp Zipformer-30M vào `main` + nút Nghe kỹ (build xanh, chạy Redmi)
- [x] Test pipeline m1–m3 + Nghe kỹ trên Redmi (WAV, không mic)
- [x] UI: ghim→mic, 3 nút → nhóm icon trong thẻ (build xanh, chụp Redmi)
- [ ] Founder: nói thật 1 câu trên Redmi, xem chữ cuối sau `aiClean`
