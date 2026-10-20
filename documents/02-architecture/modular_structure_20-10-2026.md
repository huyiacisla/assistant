# Thiết kế modular & quy ước tên tệp

Status: accepted 20-10-2026 (Founder chốt §8: làm dần, tên tệp có dấu chấm, chưa tách Gradle module). Gốc: `app/src/main/java/com/huyi/recap/` (2137 dòng, 18 tệp .kt, 1 module Gradle).

## 1. Nguyên tắc

| # | Nguyên tắc | Hệ quả |
|---|---|---|
| 1 | **Feature-first, layer-second**: thư mục gốc = module nghiệp vụ; trong module chia theo dạng | tìm theo tính năng, không rải 1 tính năng khắp 5 thư mục |
| 2 | **1 Gradle module** (`:app`), modular bằng package | YAGNI: 2k dòng, 1 người; tách Gradle module khi > ~10k dòng hoặc build chậm |
| 3 | Phụ thuộc 1 chiều: `feature → store → core`; `shared` không biết feature | tránh vòng; kiểm bằng ast-grep (§7) |
| 4 | Overlay = View thuần, tách khỏi Compose | `ComposeView` trong `WindowManager` lỗi lifecycle |
| 5 | Tên tệp `<name>.<dạng>.kt` | đọc tên biết vai trò, `ctx_glob *.screen.kt` |

## 2. Cây thư mục đề xuất

```
com/huyi/recap/
├─ app/                      # điểm vào, wiring
│  ├─ main.activity.kt            (← MainActivity.kt)
│  └─ app.nav.kt                  (điều hướng tab/trang)
├─ core/                     # không phụ thuộc feature
│  ├─ theme/   recap.theme.kt  tok.theme.kt             (← ui/Theme.kt)
│  ├─ icon/    ic.icon.kt                               (← ui/Ic.kt)
│  ├─ util/    time.util.kt  text.util.kt  fts.util.kt  (ftsQuery, parseWhen, dayOf/dayLabel)
│  └─ perm/    overlay.perm.kt  mic.perm.kt  logs.perm.kt
├─ store/                    # dữ liệu, 1 nguồn sự thật
│  ├─ db.store.kt                 (Db + migration)
│  ├─ item.entity.kt  folder.entity.kt  item.fts.kt
│  ├─ kho.dao.kt
│  ├─ item.repo.kt                (inPage(), lọc/nhóm — hiện nằm lẫn trong UI)
│  └─ prefs.store.kt              (DataStore/SharedPrefs khi cần)
├─ shared/                   # thành phần dùng chung giữa feature
│  ├─ ui/   pill.component.kt  daystrip.component.kt  bottomnav.layout.kt  chip.component.kt
│  └─ model/ page.model.kt  kind.model.kt  status.model.kt
├─ feature/                  # mỗi thư mục = 1 module nghiệp vụ
│  ├─ note/      note.screen.kt  note.card.kt  timeline.layout.kt  notefilter.state.kt
│  ├─ quicknote/ quicknote.activity.kt  quicknote.sheet.kt  quicknote.process.kt  (← VoiceActivity)
│  ├─ voice/     whisper.engine.kt  voice.process.kt        (+ cpp/)
│  ├─ capture/   capture.activity.kt  capture.service.kt  shot.process.kt  shot.a11y.kt
│  ├─ clipboard/ clip.activity.kt  clip.process.kt
│  ├─ remind/    remind.scheduler.kt  remind.receiver.kt
│  ├─ ai/        deepseek.client.kt  understand.process.kt  (← Ai.kt)
│  ├─ recap/     recap.screen.kt  recap.chart.kt
│  ├─ organize/  organize.rule.kt  organize.screen.kt
│  └─ kho/       kho.screen.kt   (host: tab + bottomBar, ghép note/recap/…)
└─ overlay/                  # View thuần, không Compose
   ├─ overlay.service.kt  bubble.view.kt  menu.view.kt  overlay.process.kt
```

Tài nguyên (`res/`, không dùng dấu chấm trong tên): `drawable/ic_<name>.xml` · `values/strings.xml` · `xml/accessibility_config.xml`. C++: `src/main/cpp/`.

## 3. Bảng "dạng" (hậu tố) — `<name>.<dạng>.kt`

> Đề xuất chỉnh bảng + quy ước class/hàm/glossary: [naming_convention](naming_convention_20-10-2026.md) (draft).

| Dạng | Vai trò | Ví dụ | Quy tắc |
|---|---|---|---|
| `screen` | màn hình đầy đủ (Compose) | `note.screen.kt` | 1 màn = 1 tệp; nhận state + callback |
| `layout` | khung bố cục bọc nhiều phần | `timeline.layout.kt`, `bottomnav.layout.kt` | không chứa logic dữ liệu |
| `component` | UI nhỏ tái dùng | `pill.component.kt` | không gọi DAO |
| `sheet` | bottom sheet / dialog | `quicknote.sheet.kt` | |
| `activity` / `service` / `receiver` | thành phần Android | `capture.service.kt` | khai ở Manifest |
| `view` | View thuần (overlay) | `bubble.view.kt` | không Compose |
| `state` | state holder / ViewModel nhẹ | `notefilter.state.kt` | không import UI |
| `process` | chuỗi xử lý nhiều bước (use-case) | `shot.process.kt` (chụp→OCR→lưu) | gọi `store`, `engine`, `client` |
| `engine` | bọc thư viện nặng/native | `whisper.engine.kt` | 1 API hẹp |
| `client` | gọi mạng | `deepseek.client.kt` | khoá từ `BuildConfig`, timeout |
| `entity` / `dao` / `repo` / `store` | dữ liệu | `item.entity.kt` | chỉ `store/` được chạm Room |
| `model` | kiểu thuần (enum, data class) | `kind.model.kt` | không phụ thuộc Android |
| `util` | hàm thuần | `time.util.kt` | có test JUnit |
| `rule` | luật phân loại | `organize.rule.kt` | thuần, test được |
| `theme` / `icon` | token, icon | `tok.theme.kt` | |
| `perm` | xin/kiểm quyền | `overlay.perm.kt` | |
| `nav` | điều hướng | `app.nav.kt` | |

Quy tắc đặt tên: `<name>` thường, một từ (nối bằng liền, không `_`); dạng ∈ bảng trên; class bên trong PascalCase (`NoteScreen`, `ItemEntity`). Thêm dạng mới = thêm 1 hàng vào bảng này.

Lưu ý kỹ thuật: Kotlin cho phép tên tệp có dấu chấm (facade class thành `Note_screenKt`); ktlint rule `filename` phải tắt nếu dùng (hiện chưa dùng ktlint). Khai `@file:JvmName("NoteScreen")` nếu cần gọi từ Java/Manifest — thường không cần.

## 4. Thành phần đề xuất thêm (chưa có trong code)

| Thành phần | Dạng | Lý do | Mức |
|---|---|---|---|
| `item.repo` | repo | gom `inPage()`/lọc/nhóm đang nằm trong `KhoScreen` (413 dòng) → UI mỏng | ● làm trước |
| `*.state` | state | tách state khỏi composable (noteQ, lọc, nhóm); có thể ViewModel khi cần xoay màn | ● |
| `page.model`, `kind.model`, `status.model` | model | thay chuỗi/số thần kỳ (`type`, `kind`, `status`, page 0–4) bằng enum | ● |
| `core/util` | util | gom `parseWhen`, `ftsQuery`, `dayOf`, `dayLabel` → test JUnit | ● |
| `core/perm` | perm | quyền overlay/mic/READ_LOGS/trợ năng hiện rải rác | ◐ |
| `shared/ui/component` | component | `Pill`, `Chip`, `DayStrip` dùng ở ≥ 2 nơi | ● |
| `*.process` | process | tách chuỗi nghiệp vụ khỏi Activity/Service (VoiceActivity 272 dòng) | ● |
| `app.nav` | nav | KhoScreen đang ôm điều hướng + dữ liệu | ◐ |
| `core/log` (`Recap` tag) | util | log thống nhất, không ghi nội dung nhạy cảm | ◐ |
| `core/di` | — | DI thủ công: 1 `AppGraph` khi > ~10 class | ○ chưa |
| Gradle module riêng | — | `:core`, `:store` | ○ chưa (§1.2) |
| `test/` mirror | test | `util`, `rule`, `fts` có test JUnit | ● |

## 5. Ranh giới & phụ thuộc

| Từ \ Tới | core | store | shared | feature | overlay |
|---|---|---|---|---|---|
| core | — | ○ | ○ | ○ | ○ |
| store | ● | — | ○ | ○ | ○ |
| shared | ● | ◐ (model) | — | ○ | ○ |
| feature | ● | ● | ● | ◐ (qua `process`/intent) | ○ |
| overlay | ● | ◐ (chỉ qua `process`) | ○ | ◐ (mở Activity bằng Intent) | — |
| app | ● | ● | ● | ● | ● |

● được · ◐ hạn chế · ○ cấm.

## 6. Ánh xạ từ hiện tại

| Hiện tại | Đích | Việc | Đã chuyển (20-10-2026) |
|---|---|---|---|
| `MainActivity.kt` | `app/main.activity.kt` | di chuyển | ✓ |
| `data/Db.kt` | `store/{db.store,item.entity,folder.entity,item.fts,kho.dao}.kt` | tách 1 tệp → 5 | ✓ |
| `ui/Theme.kt` | `core/theme/` | tách token | ✓ |
| `ui/Ic.kt` | `core/icon/ic.icon.kt` | di chuyển | ✓ |
| `ui/KhoScreen.kt` (413) | `feature/kho/kho.screen.kt` + `feature/note/*` + `store/item.repo.kt` + `*.state` | tách lớn nhất | ✓ (+`item.repo`, enum `shared/model/*`) |
| `ui/Timeline.kt` (234) | `shared/ui/{pill,daystrip}.component.kt`, `bottomnav.layout.kt`, `feature/note/note.card.kt` | tách | ✓ (`Pill` `Chip` `DayStrip` `BottomNav`, `timeline.layout`, `note.card`) |
| `ui/RecapScreen.kt` | `feature/recap/recap.screen.kt` | di chuyển | ✓ |
| `ui/Organize.kt` | `feature/organize/organize.rule.kt` | di chuyển | ✓ |
| `capture/VoiceActivity.kt` (272) | `feature/quicknote/{activity,sheet,process}` | tách | ✓ (Sheet/DateTimeDialog → `quicknote.sheet`; chep/transcribe/save → `quicknote.process`; class + state + ghi âm giữ `quicknote.activity`) |
| `capture/Whisper.kt` | `feature/voice/whisper.engine.kt` | di chuyển | ✓ |
| `capture/Ai.kt` | `feature/ai/{deepseek.client,understand.process}` | tách | ✓ |
| `capture/Remind.kt` | `feature/remind/` | tách scheduler/receiver | ✓ |
| `capture/Clip.kt` | `feature/clipboard/` | tách activity/process | ✓ |
| `capture/{Capture*,Shot*}` | `feature/capture/` | di chuyển | ✓ |
| `overlay/OverlayService.kt` (308) | `overlay/{service,bubble.view,menu.view}` | tách view khỏi service | ✓ (`bubble.view` = bubble+snap; `menu.view` = menu+pressable; `undoBar`/`stepsCard` ở lại service) |

## 7. Kiểm khả thi & lộ trình (không đổi hành vi)

Kế hoạch thực thi chi tiết (đợt 0–9, cổng, rủi ro): [phase_9_modular](../04-phases/phase_9_modular_20-10-2026.md).

| Bước | Việc | Cổng |
|---|---|---|
| 0 | Đổi package + tên tệp bằng ctx_patch/mv, **Manifest + import** cập nhật cùng lúc | build xanh |
| 1 | `core/` (theme, icon, util) + test `util` | JUnit xanh |
| 2 | `store/` tách Db.kt; **giữ nguyên schema/version** | cài đè `install -r`, Kho cũ còn nguyên |
| 3 | `shared/` (Pill, Chip, DayStrip, BottomNav) | ảnh máy thật = trước refactor |
| 4 | `feature/note` + `item.repo` + enum model | luồng Note/lọc/nhóm giống cũ |
| 5 | `feature/quicknote`, `voice`, `ai`, `remind`, `clipboard`, `capture` | quay thoại, nhắc, chụp chạy |
| 6 | `overlay/` tách view | nút nổi + menu chạy |
| 7 | Gate phụ thuộc: ast-grep cấm `store` import `feature`, `core` import `feature/store`; semgrep quyền | 0 vi phạm |

Rủi ro: đổi package class khai ở Manifest (Activity/Service/Receiver) → `ClassNotFound` runtime; Room schema không đổi nên an toàn nếu giữ `@Database`/entity name; từng bước 1 commit-able. Mỗi bước = 1 vòng workflow ([vibecoding_step_by_step](../00-guides/vibecoding_step_by_step_20-10-2026.md)).

## 8. Quyết định của Founder (20-10-2026)

| Câu hỏi | Quyết định | Quy tắc áp dụng |
|---|---|---|
| Tên tệp | `<name>.<dạng>.kt` (có dấu chấm) | tắt ktlint `filename` nếu thêm ktlint |
| Cách refactor | **Làm dần** | tính năng/tệp mới đặt đúng cấu trúc §2 ngay; tệp cũ chỉ chuyển khi chạm sửa (1 tệp = 1 bước build + kiểm); không refactor hàng loạt |
| Gradle module | chưa tách | xét lại khi > ~10k dòng |

> 20-10-2026: package đã = thư mục (Đợt 10, phase 9); #17 #21 #23 xong, #22 hoãn — xem [naming_convention](naming_convention_20-10-2026.md).

Khi chuyển 1 tệp cũ: cập nhật package + import + Manifest cùng lúc; tick cột "Đã chuyển" ở §6 (thêm cột khi bắt đầu chuyển tệp đầu tiên).
