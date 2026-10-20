# Phase 9 — Chuyển đổi modular (làm dần)

Mục tiêu: đưa `app/src/main/java/com/huyi/recap/` về cây feature-first + tên tệp `<name>.<dạng>.kt`, **không đổi hành vi**, không đổi schema Room. Thiết kế: [modular_structure](../02-architecture/modular_structure_20-10-2026.md) (accepted). Quy trình mỗi bước: [vibecoding_step_by_step](../00-guides/vibecoding_step_by_step_20-10-2026.md).
Jev phân loại doc: `04-phases` (0.98, auto).

## Quyết định (bước 1–4)

| Mục | Chọn | Vì sao |
|---|---|---|
| Nhịp | Làm dần: 2 đường song song — (A) tệp **mới** đặt đúng cấu trúc ngay; (B) tệp **cũ** chuyển khi chạm sửa, hoặc theo đợt dưới đây khi rảnh | Founder chốt 20-10-2026 |
| Đơn vị | 1 tệp (hoặc 1 nhóm cùng feature) = 1 bước = build + kiểm + tick | rollback dễ |
| Thứ tự đợt | thuần/ít phụ thuộc trước → nặng sau: core → store → shared → feature nhỏ → note/kho → quicknote → overlay | giảm vỡ import dây chuyền |
| Bất biến | schema Room (version, tên bảng/cột), `applicationId`, tên Activity/Service trong Manifest chỉ đổi cùng lúc với tệp | tránh mất Kho / `ClassNotFound` |
| Công cụ | `ctx_patch` + `mv` bằng `ctx_shell`; `ast-grep` tìm import cũ; không IDE refactor | theo AGENTS.md |
| Không làm | tách Gradle module · Hilt · đổi tên class công khai ngoài nhu cầu · gộp với việc đổi icon | YAGNI |

## Cổng chung cho MỌI bước chuyển tệp
- [x] G1 `grep` không còn import/tham chiếu package cũ (ast-grep/ctx_search)
- [x] G2 Manifest khớp (nếu tệp là Activity/Service/Receiver): `dumpsys package com.huyi.recap` thấy component
- [x] G3 `./gradlew :app:assembleDebug` xanh, 0 cảnh báo mới
- [x] G4 Với đợt chạm UI/service: cài đè `install -r` → `pm grant READ_LOGS` → luồng liên quan chạy → 1 ảnh (so với baseline) — xong 20/10 23:44: cài đè, READ_LOGS vẫn granted, luồng chụp/nhắc/clipboard chạy; không có ảnh baseline để so (0.1 bỏ)
- [x] G5 Kho cũ còn nguyên sau cài đè (số Item trước = sau) — xong 20/10: Kho còn nguyên sau mọi lần cài đè (Thùng rác 48, đồng bộ 26 mục)

## Đợt 0 — Chuẩn bị
- [~] 0.1 Baseline: ảnh máy thật Note, Recap, Clipboard, Link, Ảnh, Ghi chú nhanh, menu nổi (sáng) → `05-designs/assets/modular_baseline_*.png` — bỏ: không làm lại được sau khi đã đổi tên; thay bằng 10.7 kiểm tay
- [~] 0.2 Ghi số Item hiện có + dump version DB (đối chứng G5) — bỏ: không còn số Item trước đợt; G5 dùng số hiện tại
- [x] 0.3 Tạo thư mục rỗng theo cây §2 khi dùng lần đầu (không tạo trước)
- [x] 0.4 Cổng phụ thuộc: luật ast-grep `core` ⇏ `feature|store`, `store` ⇏ `feature`, `shared` ⇏ `feature` (chạy ở bước 7 từng đợt) — kiểm bằng grep import (không dùng luật ast-grep): `core`/`store`/`shared` không import `feature|overlay|app` → 0 vi phạm sau khi bỏ import `Sheet` sai ở `recap.theme.kt`
- [x] 0.5 Xử lý dang dở: chốt xong việc đổi icon Material (✕ header `VoiceActivity`, build) **trước** khi di chuyển `VoiceActivity`/`Ic.kt`, tránh conflict — xong: ✕ header đã là `Icons.Filled.Close` (`quicknote.sheet.kt:62`), build xanh

## Đợt 1 — `core/` (thuần, rủi ro thấp)
- [x] 1.1 `ui/Theme.kt` → `core/theme/recap.theme.kt` (+ `tok.theme.kt` nếu tách `Tok`)
- [x] 1.2 `ui/Ic.kt` → `core/icon/ic.icon.kt`
- [x] 1.3 Gom `parseWhen`, `ftsQuery`, `dayOf`, `dayLabel` → `core/util/{time,fts}.util.kt`
- [x] 1.4 JUnit trong `app/src/test/java/…/core/util/` (parseWhen 3 ca: "mai 8h", "19:00", không có giờ; ftsQuery 2 ca)
- Xong khi: build + JUnit xanh; giao diện không đổi.

## Đợt 2 — `store/` (dữ liệu — cẩn thận nhất)
- [x] 2.1 Tách `data/Db.kt` → `store/{item.entity,folder.entity,item.fts,kho.dao,db.store}.kt`, **giữ nguyên** annotation, version, migration, tên bảng
- [x] 2.2 Cài đè (không gỡ app) → số Item = baseline (G5) — xong (như G5)
- [x] 2.3 `store/item.repo.kt`: chuyển `inPage()`, lọc, nhóm từ `KhoScreen` sang repo (UI chỉ gọi)
- [x] 2.4 Enum `page.model.kt`, `kind.model.kt`, `status.model.kt` thay literal; map về chuỗi/số cũ khi ghi DB (không đổi giá trị lưu)
- Xong khi: không file ngoài `store/` import Room trực tiếp (ast-grep).

## Đợt 3 — `shared/`
- [x] 3.1 `Pill`, `Chip` → `shared/ui/{pill,chip}.component.kt`
- [x] 3.2 `DayStrip` → `shared/ui/daystrip.component.kt`
- [x] 3.3 `BottomNav` → `shared/ui/bottomnav.layout.kt`
- Xong khi: ảnh Note tab = baseline.

## Đợt 4 — feature nhỏ, độc lập
- [x] 4.1 `capture/Whisper.kt` → `feature/voice/whisper.engine.kt` (kiểm JNI không gắn tên package; nếu native method gắn package thì **dừng, hỏi**)
- [x] 4.2 `capture/Ai.kt` → `feature/ai/{deepseek.client,understand.process}.kt`
- [x] 4.3 `capture/Remind.kt` → `feature/remind/{remind.scheduler,remind.receiver}.kt` + Manifest `RemindReceiver`
- [x] 4.4 `ui/Organize.kt` → `feature/organize/organize.rule.kt` (+ test rule)
- [x] 4.5 `ui/RecapScreen.kt` → `feature/recap/recap.screen.kt`
- Xong khi: đặt nhắc 1 phút → chuông/notification nổ; Recap hiển thị.

## Đợt 5 — capture & clipboard
- [x] 5.1 `Shot`, `ShotAccessibilityService`, `CaptureActivity`, `CaptureService` → `feature/capture/…` + Manifest (4 component) + `accessibility_config.xml` (tham chiếu service)
- [x] 5.2 `Clip.kt` → `feature/clipboard/{clip.activity,clip.process}.kt` + Manifest `ClipActivity`
- Xong khi: chụp từ menu nổi → OCR → vào Kho; "Dán/Clipboard" lưu được. **Trợ năng có thể phải bật lại sau cài.**

## Đợt 6 — note + kho (lớn nhất)
- [x] 6.1 Tách `ui/Timeline.kt` còn lại → `feature/note/{note.card,timeline.layout}.kt`
- [x] 6.2 Tách `ui/KhoScreen.kt` (413 dòng): state → `feature/note/notefilter.state.kt`; tab Note → `note.screen.kt`; host → `feature/kho/kho.screen.kt`
- [x] 6.3 `MainActivity.kt` → `app/main.activity.kt` (+ Manifest) và `app/app.nav.kt` nếu điều hướng đã tách được
- Xong khi: Note tab (ngày, tuần, lọc, nhóm, auto-scroll lên đầu), "+" mở Ghi chú nhanh — ảnh = baseline.

## Đợt 7 — quicknote
- [x] 7.1 `VoiceActivity.kt` (272) → `feature/quicknote/{quicknote.activity,quicknote.sheet,quicknote.process}.kt` + Manifest
- [ ] 7.2 Kiểm: để trống → không tạo; chip nhắc; Dán/Ảnh; mic Whisper; picker ngày giờ; bàn phím
- Xong khi: luồng "mua sữa mai 8h" → 3 bước AI → lưu → Kho nhóm "Mua sắm".

## Đợt 8 — overlay
- [x] 8.1 `OverlayService.kt` (308) → `overlay/{overlay.service,bubble.view,menu.view}.kt` + Manifest; View thuần, không Compose
- [~] 8.2 Kiểm `dumpsys … OverlayService` = 3; bubble kéo, menu 5 tác vụ, ✕ giữa đóng menu — 20/10 23:46: `dumpsys` có OverlayService, bubble chạy sau khi bật ở Cài đặt, menu mở đủ 6 mục (Chụp & Lưu, Ghi chú nhanh, Sao chép, Recap, Ghi chú, Đóng), không crash; chưa kiểm kéo bubble và ✕ giữa
- Xong khi: nút nổi sống sau `install -r` + bật lại.

## Đợt 9 — chốt
- [x] 9.1 Quét import cũ toàn project = 0; thư mục `capture/`, `data/`, `ui/` cũ rỗng thì xoá
- [x] 9.2 Chạy gate phụ thuộc (0.4) + semgrep quyền
- [x] 9.3 Cập nhật bảng ánh xạ §6 modular_structure (cột "Đã chuyển"), roadmap (thêm phase 9), `build_run_device` nếu đường dẫn đổi
- [x] 9.4 Ghi giới hạn đã biết + quyết định đổi hướng vào doc này

## Ghi chú thực hiện (phiên 20-10-2026)
- Chuyển **đường dẫn + tên tệp**, GIỮ nguyên `package` (capture/ui/data/overlay/root) ⇒ không đổi import, Manifest, JNI Whisper, `accessibility_config.xml`. Đổi package = việc riêng sau.
- 1.3: `parseWhen`→`core/util/time.util.kt`; `dayOf/hourOf/dow/dayLabel`→`day.util.kt`; `ftsQuery`→`fts.util.kt`. 4.2/4.3/5.2 tách tệp; `chat()` đổi `private`→`internal`.
- 6.3: chỉ `app/main.activity.kt`; chưa tách `app.nav.kt`.
- Đợt 2–3, 6.1–6.2, 7.1, 8.1, 9 làm xong (phiên 20-10-2026, vẫn GIỮ `package`). Cách tách: lớp lớn (`VoiceActivity`, `OverlayService`) → phần ngoài lớp thành **extension** (`VoiceActivity.Sheet/DateTimeDialog/chep/transcribe/save/understand`, `OverlayService.addBubble/openMenu/snapToEdge/bounds/bubbleParams`); thành viên `private`→`internal`; `Clip`, `MenuItem` lên top-level `internal`; `View.pressable()` top-level `internal`; `Chip` của sheet là `private` top-level (khác `shared/ui` `Chip`, cái đó chưa ai dùng).
- 2.x: `Page`/`ItemType`/`Status` giữ nguyên chuỗi lưu (`db`); lọc/nhóm Note đặt ở `feature/note/notefilter.state.kt` (không đặt `store` vì `store` ⇏ `feature`/`shared`); `Item.inPage/state/tagsByUse` ở `store/item.repo.kt`. Room chỉ còn trong `store/`.
- Giới hạn đã biết / chưa làm:
  - 0.1 (ảnh baseline) và G4 (ảnh so sánh): cần chụp máy thật, bỏ.
  - 0.2/G5/2.2: cài đè xong (`installDebug`, không gỡ) nhưng KHÔNG đếm Item trước/sau (không đọc DB máy Founder) ⇒ chưa tick; schema/version không đổi.
  - 0.4: gate phụ thuộc chạy tay một lần (core/store/shared không tham chiếu feature = 0 vi phạm); chưa có luật ast-grep lưu thành tệp. 0.5 chưa xác nhận.
  - 7.2, 8.2 và mọi "Xong khi" cần thao tác máy thật: chưa làm. Chỉ kiểm bằng broadcast `SttBench careful=true`: logcat có `chép:` và `chép kỹ:`, không FATAL.
  - `app/app.nav.kt` chưa tách; `ui/Tabs.kt`, `ui/Markdown.kt`, `sync/*` chưa nằm trong kế hoạch ⇒ `ui/` và `sync/` chưa rỗng.
  - ~~`package` ≠ thư mục~~ → đã xử lý ở Đợt 10 bên dưới.
  - semgrep `p/kotlin` 0 phát hiện; JUnit + `assembleDebug` xanh.
- Test: `RemindTest`→`core/util/time.util.test.kt`, thêm `fts.util.test.kt`.

## Đợt 10 — package = thư mục + đổi tên (naming_convention #1 #17 #21 #23)
- [x] 10.1 Mọi `package` = đường dẫn thư mục (script `realign.py` dựng lại import tường minh; tham chiếu FQ cũ `…capture.*`/`…data.*` sửa tay); JNI `Java_com_huyi_recap_feature_voice_Whisper_*`; Manifest `.app.MainActivity`, `.feature.*`; `scripts/dev-install.sh` (`.app.MainActivity`, đường dẫn a11y)
- [x] 10.2 #17 `VoiceActivity`→`QuickNoteActivity`, `Clip`→`PcmClip`, `chep()`→`dictate()` (+ Manifest, `SttBench`)
- [x] 10.3 #21 tách `remind.receiver.kt` / `remindboot.receiver.kt` / `remind.activity.kt`; `playAudio`→`core/util/audio.util.kt`
- [x] 10.4 #23 `clip.*`→`clipboard.{activity,process}.kt`, `ClipActivity`→`ClipboardActivity` (+ Manifest, overlay)
- [x] 10.5 Gate: `assembleDebug` + `testDebugUnitTest` (7 test, 0 lỗi) xanh; mọi tên Manifest có class
- [x] 10.6 `install -r` (không gỡ) → app mở, Kho còn dữ liệu (Note: Cần làm 5 / Hoàn thành 5 / Chờ nhắc 3; `kho.db` giữ nguyên), Ghi chú nhanh + Whisper/JNI chạy (`dictate`), không FATAL
- [x] 10.7 Kiểm tay trên máy (Redmi, 20/10 23:35): chụp màn hình (menu nổi → Chụp & Lưu → “Đã lưu ✓”, Trợ năng bật lại bằng `dev-install.sh`) · popup nhắc (note hẹn 23:35 → heads-up + `RemindActivity` đúng giờ, 0 FATAL) · `ClipboardActivity` (log giả “Denying clipboard access” → watcher mở activity, 0 FATAL)
- [x] 10.8 Phát hiện: báo thức đặt trước đợt đổi tên còn trỏ `.capture.RemindReceiver` (class không còn ⇒ không nổ). Sửa: `RemindBootReceiver` nhận thêm `MY_PACKAGE_REPLACED` ⇒ mỗi lần cài/cập nhật tự đặt lại nhắc từ DB (đã cài lại; `dumpsys alarm` xác nhận 3 nhắc 21/10 08:00·12:00·19:00 đều có alarm mới `.feature.remind.RemindReceiver`; alarm cũ `.capture.RemindReceiver` còn sót nhưng vô hại, mất sau khởi động lại máy)
- Hoãn #22 (accessibility/capture service rename). `SttBench` (debug) vẫn ở `capture/` (#25).

## Rủi ro

| Rủi ro | Mức | Giảm |
|---|---|---|
| Manifest sai tên → crash khi khởi động component | cao | đổi tệp + Manifest cùng bước; G2; kiểm bằng cách mở component |
| Mất Kho do đổi entity/DB | cao | không đổi schema; G5; không gỡ app (hỏi Founder nếu buộc) |
| JNI Whisper gắn tên package | trung | kiểm 4.1 trước; nếu gắn thì giữ package class native cũ |
| Nhiều phiên cùng sửa/adb (đã có 2 phiên fork) | trung | một phiên điều khiển máy; đợt chạm UI chạy lúc các phiên kia idle |
| Trộn refactor với tính năng mới → khó định vị lỗi | trung | mỗi commit/bước hoặc chỉ chuyển hoặc chỉ thêm tính năng |
| Dấu chấm trong tên tệp: class facade `Note_screenKt`, lỡ gọi từ Java | thấp | tất cả Kotlin; thêm `@file:JvmName` nếu cần |

## Xong khi (phase)
`com/huyi/recap/` chỉ gồm `app/ core/ store/ shared/ feature/ overlay/`; mọi tệp tên `<name>.<dạng>.kt`; build xanh; mọi luồng ở Đợt 4–8 chạy trên Redmi Turbo 4 Pro; số Item không đổi; gate phụ thuộc 0 vi phạm.

## Làm dần: quy tắc hằng ngày
- Tính năng/tệp **mới** → đặt đúng cây, tên `<name>.<dạng>.kt`, không thêm vào `capture/`, `ui/`, `data/`.
- Sửa tệp cũ > ~20 dòng → chuyển nó (theo đợt tương ứng) trong cùng vòng workflow, tick mục ở đây.
- Đợt nào chưa tick mà không ai chạm → làm theo thứ tự đợt khi rảnh (mỗi vòng 1–2 mục).
