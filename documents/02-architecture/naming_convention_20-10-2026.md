# Quy ước đặt tên & naming hoá thành phần

Status: in progress 20-10-2026 — #1 #17 #21 #23 ĐÃ LÀM (build + test xanh, `install -r` máy 5d6c1a88 OK); #22 hoãn; còn lại P1/P2 làm dần. Mở rộng [modular_structure](modular_structure_20-10-2026.md) §3; không thay cấu trúc thư mục §2.

## Context

| Ranh giới | Quyết định | Ràng buộc |
|---|---|---|
| Phạm vi | tên tệp · class · hàm · package · enum · tài nguyên · id lưu bền · test | 1 module `:app`, `com.huyi.recap`, 51 tệp main + 5 test + 1 debug |
| Nguyên tắc nền | **1 khái niệm = 1 từ = 1 chỗ**; tên đọc ra vai trò; máy kiểm được (ast-grep) | không đổi hành vi |
| Đóng băng | tên đã lưu bền / máy khác đọc (§5) không đổi, chỉ ghi chú | tránh migration, mất cấu hình máy |
| Chế độ | làm dần (§8 modular): tệp mới/được chạm theo quy ước ngay | không refactor hàng loạt trừ khi Founder chọn |

## Current State

Quét trực tiếp mã (20-10-2026). `●` vi phạm · `—` sạch.

| # | Quy ước hiện có | Hiện trạng | Evidence |
|---|---|---|---|
| a | Package = đường dẫn thư mục | ● **45/51** tệp main khai package cũ (`ui` 18 · `capture` 16 · `data` 10 · root 1) dù đã nằm `core/ store/ feature/ shared/ app/` | `package com.huyi.recap.ui` trong `feature/note/note.screen.kt` |
| b | `<name>.<dạng>.kt` | ● `Tabs.kt` `Markdown.kt` `BackupWorker.kt` `DriveAuth.kt` `DriveSync.kt` `SttBench.kt` chưa đổi; `ui/` `sync/` ngoài sơ đồ | cây `app/src` |
| c | Tên tệp ↔ class chính | ● `quicknote.activity.kt` → `VoiceActivity` · `note.card.kt` → `TimelineRow` · `db.store.kt` → `Db` · `kind.model.kt` → `ItemType` · `shot.accessibility.kt` → `ShotAccessibilityService` | khai báo class |
| d | 1 tệp = 1 dạng | ● `remind.receiver.kt` có `RemindReceiver` + `RemindBootReceiver` + `RemindActivity`; `Tabs.kt` có 4 màn + TagPicker + share + IconButton | — |
| e | Dạng ∈ bảng §3 | ● dùng ngoài bảng: `card` `layout`(2 tệp không đúng nghĩa) `store`(trùng tên package) `accessibility` `scheduler` `fts` `icon` | `note.card` `timeline.layout` `db.store` |
| f | Tên đầy đủ, không viết tắt | ● `clip.*` (dir là `clipboard`) · `ic.icon` · `Tok` · `ic_launcher_fg` · `St` · `Fts` | — |
| g | 1 ngôn ngữ cho định danh | ● VN không dấu lẫn EN: `chep()` `Part.Toi/Chieu` `"buoi"/"loai"` `Page.Setting` vs `SettingTab`; `kho` (thuật ngữ miền) | `quicknote.process.kt` `timeline.layout.kt` |
| h | 1 từ = 1 nghĩa | ● `kind` = 3 nghĩa (`ItemType` · `Item.kind` do AI · `kind()` ở recap) · `Clip` = clipboard **và** đoạn PCM · `Chip` ×4 (`Chip` `FChip` `IconLabelChip` private `Chip`) · `Recap` = thương hiệu + tab + màn thống kê · `ViewScreen` vs `DetailScreen` | `understand.process.kt` `quicknote.activity.kt` |
| i | Test phản chiếu nguồn | ● `ClipLinkTest` `OrganizeTest` ở gốc; `time.util.test.kt` chứa `AiWhenTest` `AnalysisTest` (thuộc `ai/understand.process`) | cây `src/test` |
| j | Manifest | ● vẫn `.capture.*` `.MainActivity` (khớp package cũ, không khớp thư mục) | `AndroidManifest.xml` |

Kết luận: **thư mục đã đúng, tên/ngữ nghĩa chưa**. Gốc rễ 1 điểm (a): gate ast-grep §7 bước 7 ("`store` không import `feature`") **không chạy được** khi mọi import vẫn là `ui.*`/`data.*`.

## Decision

### 1. Nguyên tắc (N1–N8)

| # | Nguyên tắc | Kiểm bằng |
|---|---|---|
| N1 | **Package = đường dẫn.** `feature/note/` ⇒ `com.huyi.recap.feature.note` | ast-grep: `package` ≠ path |
| N2 | **Tệp = `<subject>.<role>.kt`**: subject = 1 từ thường đầy đủ (ghép liền ≤ 2 hình vị: `quicknote` `notefilter`); role ∈ §2; cấm viết tắt trừ allowlist `db dao fts ui id a11y` | glob `*.kt` regex |
| N3 | **Class chính = `Pascal(subject)+Pascal(role)`** cho role có hậu tố (§2 cột "Class"): `note.screen.kt`→`NoteScreen`. Role còn lại: tên danh từ = subject (`Item`, `Pill`) hoặc hàm top-level | ast-grep: class đầu tệp |
| N4 | **1 tệp = 1 role = 1 class chính.** Cho phép tách tệp lớn (> ~250 dòng) bằng extension trên class chính (`QuickNoteActivity.sheet()`) — tệp phụ cùng subject, role khác | review |
| N5 | **1 định danh = 1 ngôn ngữ**: code = tiếng Anh; chuỗi hiển thị = tiếng Việt; ngoại lệ đóng: `kho` (thuật ngữ miền Founder, UI dùng); giá trị lưu bền (slug `viec`, `"note"`) = dữ liệu, không phải định danh | grep dấu/VN: `chep` `Toi` `Chieu` |
| N6 | **1 từ = 1 nghĩa** theo glossary §3; thêm khái niệm = thêm 1 hàng | review |
| N7 | **Màn tab = `<Page>Screen`**: ∀ `Page.X` ⇒ `XScreen` (không dùng `Tab`) | ast-grep: `*Tab(` |
| N8 | **Hàm**: Composable `PascalCase` danh từ; còn lại `lowerCamel` động từ-đầu (`saveClip` `scheduleRemind` `parseWhen`); boolean `is/has/can`; hằng `SCREAMING_SNAKE`; enum member `Pascal` EN; extra Intent = `EXTRA_<NAME>` ở tệp class nhận | ktlint/ast-grep |

### 2. Bảng role (đóng, 1 hàng = 1 dạng)

| Role | Vai trò | Class (hậu tố) | Ghi chú |
|---|---|---|---|
| `screen` | màn Compose đầy đủ | ● `NoteScreen` | `Page`⇒`XScreen` (N7) |
| `sheet` | bottom sheet / dialog | ● `…Sheet` | |
| `component` | UI nhỏ tái dùng | — hàm `Pill` | không gọi DAO. **Thay `layout` và `card`** |
| `state` | state holder | ● `NoteFilterState` | không import UI |
| `theme` | token/giao diện | — `RecapTheme`, `Tokens` | |
| `activity` `service` `receiver` `worker` | thành phần Android | ● `…Activity` … | khai Manifest. **+`worker`** (WorkManager) |
| `view` | View thuần (overlay) | — hàm ext | không Compose |
| `process` | chuỗi nhiều bước (use-case) | — hàm `saveClip` | gọi `store`/`engine`/`client` |
| `rule` | luật thuần, test được | — | |
| `scheduler` | đặt/huỷ lịch hệ thống | — hàm `scheduleRemind` | **+mới** (AlarmManager) |
| `engine` | bọc thư viện native | ● `WhisperEngine` | |
| `client` | gọi mạng | ● `DeepSeekClient` | khoá từ `BuildConfig` |
| `auth` | đăng nhập/token dịch vụ ngoài | ● `DriveAuth` | **+mới** |
| `database` `entity` `dao` `fts` `repo` | dữ liệu | ● `KhoDatabase` `KhoDao`; `Item` (entity không hậu tố) | chỉ `store/` chạm Room. **`store` bỏ khỏi role** (trùng tên package) |
| `model` | enum/data class thuần | — `Page` | |
| `util` | hàm thuần | — | có test |
| `perm` `nav` | quyền · điều hướng | — | |
| `test` | `<subject>.<role>.test.kt` | ● `<Subject><Role>Test` | cùng đường dẫn nguồn (N1) |

Bỏ: `layout` (không còn khung bố cục thật), `card`, `store`, `icon`, `accessibility`. Thêm: `worker` `scheduler` `auth` `database`.

### 3. Glossary — 1 từ = 1 nghĩa

| Khái niệm | Từ chuẩn | Nghĩa | Hiện tại lẫn với |
|---|---|---|---|
| Dạng nội dung gốc | `type` / `ItemType` | note · link · image · text | `kind.model.kt` (tên tệp) |
| Nhóm do AI gán | `category` | viec · lich · mua… (`CATEGORIES`) | `Item.kind` `KINDS` `Analysis.kind` `kind()` |
| Vòng đời nhắc | `status` / `Status` | open · done · ended · snoozed | — |
| Tiến trình AI | `aiStatus` | pending · done · error | — |
| Tab điều hướng | `page` / `Page` | Note · Recap · Clipboard · Settings | `*Tab` |
| Buổi trong ngày | `part` / `Part` | Morning… (EN) | `Toi` `Chieu` |
| Toàn bộ lưu trữ | `kho` | aggregate: `KhoDatabase` `KhoDao` `KhoScreen`, file `kho.db` | — |
| Thư mục con | `folder` | `Folder`, `UNSORTED` | — |
| Clipboard hệ thống | `clipboard` (đủ chữ) | `clipboard.*` `ClipboardScreen` `Page.Clipboard` | `clip.*` `ClipTab` |
| Đoạn âm thanh PCM | `PcmClip` / `segment` | trong `quicknote` | `Clip` |
| Thương hiệu / package gốc | `recap` | `RecapTheme`, `recap.json`, `Page.Recap` (tab danh sách) | — |
| Màn thống kê | `stats` | `StatsScreen` (hiện `RecapScreen`, callback đã tên `onStats`) | `RecapScreen` |
| Chip lọc | `Chip` (+ tham số `icon`) | 1 composable ở `shared/ui` | `FChip` `IconLabelChip` `Chip`(private) |

## Interfaces — bản đồ đổi tên

Mức: `P1` chỉ đổi tên tệp (không đụng class/runtime) · `P2` đổi class/hàm (compile bắt lỗi) · `P3` đổi tên khai ở Manifest/hệ thống (rủi ro runtime §Risks).

| # | Hiện tại | Đề xuất | Lý do | Mức |
|---|---|---|---|---|
| 1 | ✅ 45 tệp `package …ui/data/capture` | package = thư mục (N1) + sửa import + Manifest | gốc rễ, mở khoá gate | P3 |
| 2 | `store/db.store.kt` `Db` | `kho.database.kt` `KhoDatabase` | role `store` trùng package; khớp `kho.db` `KhoDao` | P2 |
| 3 | `shared/model/kind.model.kt` | `itemtype.model.kt` | tên tệp = enum `ItemType` | P1 |
| 4 | `Item.kind` / `KINDS` / `Analysis.kind` | `category` / `CATEGORIES` / `Analysis.category`; cột Room giữ `@ColumnInfo(name="kind")` | glossary; **không migration** | P2 |
| 5 | `core/icon/ic.icon.kt` | `shared/ui/iconlabel.component.kt` (xoá `core/icon`) | tệp chỉ có `IconLabel`; `ic` viết tắt | P1 |
| 6 | `core/util/time.util.kt` · `day.util.kt` | `timeparse.util.kt` (`parseWhen`) · `daylabel.util.kt` (`dayOf` `dayLabel`) | `time`/`day` mơ hồ | P1 |
| 7 | `feature/note/timeline.layout.kt` | `timeline.component.kt`; `Part`/`partOf`/`isLive`/`statGroup` tách `timeline.model.kt` khi chạm | không còn khung bố cục; lẫn logic | P1 |
| 8 | `shared/ui/bottomnav.layout.kt` | `bottomnav.component.kt` | bỏ role `layout` | P1 |
| 9 | `feature/note/note.card.kt` | `timelinerow.component.kt` (`TimelineRow`) + `swipecard.component.kt` (`SwipeCard`) sang `shared/ui` | 1 tệp 1 class chính | P1 |
| 10 | `Part.Toi/Chieu…` · key `"buoi"/"loai"` | `Part.Night/Afternoon…` · `"part"/"type"` | N5 | P2 |
| 11 | `Page.Clip/Setting` · `RecapTab/ClipTab/SettingTab` | `Page.Clipboard/Settings` · `RecapScreen/ClipboardScreen/SettingsScreen` (N7) | `Tab` ≠ `Screen` | P2 |
| 12 | `RecapScreen` (thống kê) | `StatsScreen` → `feature/stats/stats.screen.kt` | giải xung đột #11 | P2 |
| 13 | `ui/Tabs.kt` (316 dòng) | tách: `feature/recap/recap.screen.kt` · `feature/clipboard/clipboard.screen.kt` · `feature/settings/settings.screen.kt` · `shared/ui/tagpicker.component.kt` · `shared/ui/iconbutton.component.kt` (`IconButton` đang che Material) · `core/util/share.util.kt` · `ViewScreen`⇄`DetailScreen` gộp/đặt tên `ItemDetail`/`ItemView` | N2 N4 | P2 |
| 14 | `ui/Markdown.kt` | `core/util/markdown.util.kt` (`wrap` `linePrefix` `toggleTaskAt`, test được) + `shared/ui/markdown.component.kt` (`Markdown` `mdTransform`) | pure vs UI | P1 |
| 15 | `Chip` `FChip` `IconLabelChip` private `Chip` | 1 `Chip` ở `shared/ui/chip.component.kt` | N6 | P2 |
| 16 | `feature/kho/detail.screen.kt` | `itemdetail.screen.kt` (`ItemDetailScreen`) | `detail` mơ hồ | P1 |
| 17 | ✅ `quicknote.activity.kt` `VoiceActivity` · `Clip` · `chep()` | `QuickNoteActivity` · `PcmClip` · `dictate()` | N3 N5 N6 | **P3** |
| 18 | `voice/` `Whisper` `Zipformer` (object) | `WhisperEngine` `ZipformerEngine` | N3 | P2 |
| 19 | `ai/deepseek.client.kt` `chat()` | `object DeepSeekClient { chat }` | N3 | P2 |
| 20 | `understand.process.kt` `When` `Brief` `Analysis` | `RemindWhen` · `AiBrief` · `AiAnalysis` | `When` mơ hồ | P2 |
| 21 | ✅ `remind.receiver.kt` (3 class) | `remind.receiver.kt` · `remindboot.receiver.kt` · `remind.activity.kt`; `playAudio`→`core/util/audio.util.kt` | N4 | **P3** |
| 22 | `capture.service.kt` · `shot.accessibility.kt` · `capture.activity.kt` | `projection.service.kt` (`ProjectionService`) · `a11y.service.kt` (`A11yService`) · `projection.activity.kt`; `shot.process.kt` giữ | tên theo cơ chế (MediaProjection vs Accessibility); hết `Capture…` ×2 | **P3** ★ |
| 23 | ✅ `clip.activity.kt` `clip.process.kt` | `clipboard.activity.kt` (`ClipboardActivity`) · `clipboard.process.kt` | N2 (không viết tắt) | **P3** |
| 24 | `sync/BackupWorker.kt` `DriveAuth.kt` `DriveSync.kt` | `feature/sync/backup.worker.kt` · `drive.auth.kt` · `drive.client.kt` (`DriveClient`) | vào `feature/` | P2 |
| 25 | `debug/…/capture/SttBench.kt` | `debug/…/feature/voice/sttbench.receiver.kt` | mirror source set | P3 (debug) |
| 26 | `core/theme/recap.theme.kt` `Tok` | giữ tệp; `Tok`→`Tokens` | viết tắt công khai; **bỏ `tok.theme` khỏi sơ đồ** | P2 |
| 27 | `res/drawable/ic_launcher_fg.xml` | `ic_launcher_foreground.xml` (+ sửa `mipmap-anydpi-v26/ic_launcher.xml`) | viết tắt | P1 |
| 28 | Test: `ClipLinkTest` · `OrganizeTest` · `time.util.test.kt` (3 class) · `FtsTest` | `feature/clipboard/clipboard.process.test.kt` (`ClipboardProcessTest`) · `feature/organize/organize.rule.test.kt` · `RemindTest`→`core/util/timeparse.util.test.kt`, `AiWhenTest`+`AnalysisTest`→`feature/ai/understand.process.test.kt` · `fts.util.test.kt` `FtsUtilTest` | N1 N3 | P1 |

★ Chọn tên theo cơ chế vì `CaptureService`/`CaptureActivity`/`ShotAccessibilityService` cùng nghĩa "chụp" ⇒ khó phân biệt; đổi mức P3 nên có thể hoãn (Founder §7).

Sửa sơ đồ [modular §2](modular_structure_20-10-2026.md): `shot.a11y`→`a11y.service` · bỏ `tok.theme` `prefs.store` · `ic.icon`→`iconlabel.component` · thêm `feature/sync` `feature/stats` `feature/settings` · `layout`→`component`.

## Security & Data

N/A — không đổi quyền/dữ liệu. Lưu ý **định danh lưu bền đóng băng** (không đổi, chỉ áp quy ước cho cái mới):

| Định danh | Giá trị hiện có | Vì sao đóng băng | Quy ước cho mới |
|---|---|---|---|
| Tệp DB | `kho.db` | đổi = mất Kho | — |
| Cột Room | `type` `kind` `aiStatus`… | đổi cần migration | tên field Kotlin đổi, `@ColumnInfo(name)` giữ |
| Trigger | `item_touch` | schema | `<table>_<verb>` |
| Slug dữ liệu | `viec` `lich` `note` `link` | đã lưu trong Kho + Drive | slug EN kebab cho mới |
| SharedPrefs / Work | `drive` · `drive_backup` | đổi = mất `last`, nhân đôi job | `<feature>` · `<feature>_<job>` |
| Notification channel | `capture` `remind` `overlay` | đổi = mất cấu hình mức ưu tiên đã chỉnh trên máy | `<feature>` |
| Tệp Drive | `recap.json` | máy khác đọc | — |
| Extra Intent | `code` `data` `id` `text` | nội bộ, đổi được cùng lúc | `const val EXTRA_<NAME>` |

## Risks & Tradeoffs

| Check | ID | Risk | Status | Owner | Updated |
|---|---|---|---|---|---|
| [ ] | R1 | Đổi tên class khai Manifest ⇒ `ClassNotFound` nếu sót 1 chỗ | mitigate: ast-grep + `build` + cài đè chạy từng luồng | Founder | 20-10-2026 |
| [ ] | R2 | **Đổi `ShotAccessibilityService`** ⇒ Android bỏ trạng thái "đã bật" ⇒ phải bật lại trong Cài đặt › Trợ năng (HyperOS còn tự tắt) | chấp nhận hoặc hoãn #22 | Founder | 20-10-2026 |
| [ ] | R3 | Báo thức đã đặt trỏ `RemindReceiver` cũ; đổi package ⇒ alarm cũ rơi | cài đè rồi mở app (boot receiver đặt lại) hoặc đặt lại nhắc | Founder | 20-10-2026 |
| [ ] | R4 | Đổi field Room `kind`→`category` quên `@ColumnInfo` ⇒ schema đổi ⇒ crash/mất dữ liệu | giữ `name="kind"`; kiểm `install -r` Kho cũ còn nguyên | Founder | 20-10-2026 |
| [ ] | R5 | Đổi package hàng loạt xung đột với "làm dần" | làm 1 đợt riêng (P3 #1) có cổng build, hoặc hoãn tới khi chạm | Founder | 20-10-2026 |
| [ ] | R6 | Quy ước quá nặng cho 4k dòng 1 người | giữ: bảng role đóng, glossary 13 hàng, 8 nguyên tắc; không thêm tool ngoài ast-grep | — | 20-10-2026 |

Tradeoff chính: **tên có dấu chấm** (đã chốt) đổi lấy đọc-vai-trò-từ-tên; trả giá facade `Note_screenKt` + tắt ktlint `filename`. Giữ.

## Verification

| Kiểm | Lệnh | Kết quả | Evidence |
|---|---|---|---|
| Package = path | ast-grep/script: so `package` với đường dẫn | 0 lệch | (chưa chạy) |
| Tên tệp ∈ `<subject>.<role>.kt`, role ∈ §2 | `ctx_glob 'app/src/**/*.kt'` + regex | 0 tệp ngoài bảng | (chưa chạy) |
| Không còn VN không dấu trong định danh | grep `chep|Toi|Chieu|buoi|loai` ngoài slug | 0 | (chưa chạy) |
| Không còn `*Tab(` | ast-grep | 0 | (chưa chạy) |
| Hành vi không đổi | `./gradlew testDebugUnitTest assembleDebug`; `install -r`; Kho cũ còn; thoại · nhắc · chụp · clipboard chạy | xanh | (chưa chạy) |

## Related

| Document ID | Quan hệ |
|---|---|
| [modular_structure_20-10-2026](modular_structure_20-10-2026.md) | mở rộng §3 (bảng dạng); sửa sơ đồ §2 theo §Interfaces |
| [phase_9_modular](../04-phases/phase_9_modular_20-10-2026.md) | đợt thực thi; thêm đợt "naming" nếu Founder chọn |

## 7. Việc cần Founder chốt

| # | Câu hỏi | Khuyến nghị | Thay thế |
|---|---|---|---|
| 1 | ✅ Căn package theo thư mục (#1)? | **Có, 1 đợt riêng** — mở khoá gate ast-grep; cơ học | Không: giữ package phẳng, gate chỉ theo đường dẫn tệp |
| 2 | Đổi tên có rủi ro Manifest/trợ năng (#17 #21 #22 #23)? ✅ #17 #21 #23 xong, #22 hoãn | **Hoãn #22 (R2)**; làm #17 #21 #23 cùng đợt #1 | Làm hết một lần |
| 3 | `kho` giữ làm thuật ngữ miền? | **Giữ** (UI dùng, tránh churn) | Đổi `vault`/`library` |
| 4 | Phần P1/P2 còn lại | làm dần khi chạm tệp (đã chốt §8) | gom 1 đợt |
