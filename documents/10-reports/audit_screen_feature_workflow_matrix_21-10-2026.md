# Audit — Ma trận màn hình × tính năng × workflow (chỉ dựa vào code)

Status: done · Ngày: 21-10-2026 · Phân loại: `10-reports` (audit)
Phương pháp: 3 agent đọc code song song (UI · capture/remind/voice · dữ liệu/AI/sync); tôi tự kiểm lại 4 điểm then chốt; Jev `rerank` xếp hạng 26 lỗi. **Không có thiết bị thật** ⇒ mục phụ thuộc HyperOS/quyền/hiệu năng ghi "chưa kiểm được".
Ký hiệu: ● đúng theo code · ◐ một phần/mong manh · ✗ bug · ○ stub/chưa có.
Liên quan: [release_build_bugs](../07-issues/bugs/release_build_bugs_21-10-2026.md) · [roadmap](../03-plans/roadmap_20-10-2026.md)

## 1. Đã tự kiểm (không chỉ tin agent)
| Điểm | Kết quả | Bằng chứng |
|---|---|---|
| Bubble lọt vào ảnh chụp A11y | ✗ xác nhận | `menu.view.kt:74` `it.act(); closeMenu()`; `act` ẩn bubble (L38) rồi `closeMenu` đặt `VISIBLE` (`overlay.service.kt:97`) trước khi `shoot()` chạy sau 250 ms |
| AI cắt 500 ký tự | ✗ xác nhận | `deepseek.client.kt:19` `user.take(500)` dùng cho mọi lệnh; `aiClean` (`understand.process.kt:44`) thay transcript dài bằng bản cụt |
| OCR bản bundled | ● | `libs.versions.toml:22` `com.google.mlkit:text-recognition` (không phải `play-services-…`) ⇒ chạy được trên ROM CN |
| Drive backup ghi đè | ✗ xác nhận | `DriveSync.kt:32` `update(recap.json)` bằng DB cục bộ, không gộp |

## 2. Ma trận màn hình × tính năng × workflow
Workflow = BẮT ĐẦU → bước chính → KẾT QUẢ CUỐI.

### 2.1 Bề mặt nổi (overlay)
| Màn hình | Tính năng | Bắt đầu → … → Kết quả cuối | TT |
|---|---|---|---|
| Bubble | Kéo thả, hít mép, chạm mở menu | kéo ≥16px → snap 150 ms → toạ độ mới / chạm → toggle menu | ◐ animator không huỷ khi destroy |
| Menu | 5 tác vụ + Đóng | chạm bubble → `openMenu` → `act()` rồi `closeMenu()` | ● |
| Menu | Chụp & Lưu (A11y) | chạm → ẩn bubble → 250 ms → `takeScreenshot` → `saveShot` → Item(ảnh) + undo | ✗ bubble lọt ảnh |
| Menu | Chụp & Lưu (MediaProjection dự phòng) | chạm (không có A11y) → `CaptureActivity` consent → `CaptureService.grab` → `saveShot` | ◐ không timeout, rò FGS |
| Undo bar | Snackbar 5 s + Hoàn tác | `saveShot/save/saveClip` → `showUndo` → xoá Item nếu bấm | ◐ không xoá file ảnh/wav |
| Steps card | Tiến trình 5 bước | `showSteps` → cập nhật n → tự gỡ 20 s | ● |
| Clipboard watcher | Phát hiện copy ở mọi app | `watchClipboard` đọc logcat "Denying clipboard access" → `ClipboardActivity` → `saveClip/saveShot` | ◐ cần `READ_LOGS` bằng adb, process logcat không kill; lưu cả mật khẩu |

### 2.2 Ghi chú nhanh & nhắc giờ
| Màn hình | Tính năng | Workflow | TT |
|---|---|---|---|
| QuickNote sheet | Ghi âm → chữ | chạm mic → `AudioRecord` 16k → tự dừng khi im → Zipformer/Whisper → `aiClean` → clip | ✗ không dừng ở 60 s; ✗ `aiClean` cắt >500 ký tự |
| QuickNote sheet | Nghe kỹ, sửa/xoá/gộp clip | chạm → Whisper lại → thay chữ | ◐ JNI buf 8192 cắt UTF-8 |
| QuickNote sheet | Chọn ngày giờ | chip → DatePicker → TimePicker → `chosen` | ◐ giờ quá khứ không báo |
| QuickNote sheet | Lưu | ✓ → `remindAt` → AI (recap/giờ/loại/tiêu đề) → `insert` → `scheduleRemind` → undo | ● (gọi `aiWhen` 2 lần) |
| Parse giờ | "mai 9h họp" → remindAt | `parseWhen` (luật) → `aiWhen` dự phòng | ◐ "20 hộp"→20:00; "12 giờ tối"→12:00 |
| Báo thức | Đặt/huỷ | `setAlarmClock` + PendingIntent IMMUTABLE | ● |
| Thông báo nhắc | Nổ đúng giờ | `RemindReceiver` → notification HIGH + fullScreenIntent | ◐ chưa kiểm `canUseFullScreenIntent` (API 34+) |
| RemindActivity | Xong / Hoãn 1 giờ / Đóng | popup → cập nhật status + đặt lại alarm | ◐ thiếu `taskAffinity=""` |
| Boot receiver | Đặt lại báo thức sau khởi động | BOOT_COMPLETED → `dao.upcoming` → `scheduleRemind` | ◐ `exported=true` thừa; HyperOS có thể chặn autostart |

### 2.3 Ứng dụng chính (4 tab)
| Màn hình | Tính năng | Workflow | TT |
|---|---|---|---|
| Host (Kho) | Điều hướng, `onNewIntent` | menu nổi → intent `go` → `nav++` → dựng lại | ◐ mất state; Back ở Thùng rác/Thống kê thoát app |
| Host | Xoá mềm + Hoàn tác | `del` → `cancelRemind` + `softDelete` → snackbar → `restoreItem` | ● |
| Host | Dọn thùng rác 30 ngày | mở Kho → `expired` → `purge` | ◐ chỉ khi mở app |
| Host | AI phân loại nền | item `pending` → `aiAnalyze` → `update(kind)` | ◐ lỗi → `error` vĩnh viễn; ghi đè snapshot cũ |
| Note | Danh sách theo ngày/tuần, nhóm Buổi/Loại | `DayStrip` → lọc → `TimelineRow` | ● |
| Note | Lọc (loại, tag, buổi, tìm) | sheet → `NoteFilterState` | ◐ chỉ trong 1 tuần, không khớp title/summary, mất khi xoay |
| Note | Quẹt phải = Xong/Mở lại; trái = Xoá + Undo; chạm = Chi tiết; giữ = menu | `SwipeToDismissBox` → `setStatus`/`del` | ● |
| Note | Phát ghi âm, thumb ảnh | loa → `MediaPlayer`; `produceState` IO | ◐ `File.exists()` trên main |
| Recap tab | Lịch + chip loại, nhóm buổi, chia sẻ | `DayStrip` → `shown` | ● (chip "chưa phân loại" thiếu) |
| Thống kê | Tổng/loại/top tag, chép, chia sẻ | `RecapScreen` | ◐ "AI tóm tắt" ○ stub (phase 5) |
| Xem chi tiết (Recap) | Ảnh + toàn văn + share | `viewing` → `ViewScreen` | ✗ decode ảnh trên main; giữ Item cũ |
| Clipboard tab | Lọc Ảnh/Link/Văn bản, nhóm ngày | `FChip` → `groupBy` | ◐ chip Ảnh gần như luôn 0 |
| Clipboard tab | Lưu clipboard hiện tại | nút → `readClip` → `saveClip` | ◐ dedupe toàn cục, im lặng khi trùng |
| Chi tiết | Autosave, Markdown, toolbar, tick ☑, tag, nhắc, ảnh | state đổi → 600 ms → `persist` | ● (copy ảnh trên main) |
| Thùng rác | Khôi phục / Xoá vĩnh viễn | quẹt/nút → `restoreItem`/`purge` | ✗ xoá vĩnh viễn không xác nhận; nút 38 dp |
| Cài đặt | Google Drive kết nối/sao lưu/khôi phục/tự động | `authorize` → `onToken` → `DriveSync` | ✗ ghi đè, restore chỉ thêm, không timeout |
| Cài đặt | Đồng bộ JSON | `exportJson` → `ACTION_SEND` | ✗ cắt 200k ký tự im lặng |
| Cài đặt | Nút nổi (công tắc) | `startBubble` / `stopService` | ◐ trạng thái không refresh sau cấp quyền |
| Cài đặt | Thông báo, Thùng rác | mở cài đặt hệ thống / màn Thùng rác | ● (Flow tạo lại mỗi recompose) |
| Bottom nav | 4 tab + "+" | `onTab` / "+" → QuickNote | ◐ thiếu `Role.Tab`, contentDescription |
| Sắp xếp kho | Gom theo tag chung | `go=organize` → `suggest` → "Gom" | ◐ chỉ vào được qua intent, không đóng dialog |

### 2.4 Dữ liệu · AI · đồng bộ (lớp dưới)
| Tính năng | Workflow | TT |
|---|---|---|
| Lưu Item | `save/saveShot/saveClip` → `insert` (uid, updatedAt) → alarm | ● |
| FTS | `ftsQuery` → `dao.search` | ◐ không index title/summary, `đ`≠`d` |
| Migration v1→9 | chuỗi ALTER + trigger | ◐ `exportSchema=false`, không test |
| AI recap/giờ/loại/tiêu đề | `chat` DeepSeek JSON | ● (cắt 500 ký tự; tag có dấu bị phá) |
| Tự dọn thùng rác | — | ○ chưa có WorkManager |
| Drive restore | tải json → dedupe uid → insert | ◐ không `scheduleRemind` lại, thiếu cột, không transaction |

## 3. Workflow đầu → cuối của luồng chính
| Luồng | Đầu | Cuối | Điểm gãy theo code |
|---|---|---|---|
| Chụp → Kho | chạm Chụp & Lưu | Item(ảnh, OCR, tóm tắt, loại) + undo | bubble lọt ảnh; MP không timeout; nén JPEG trên main |
| Giọng → nhắc | chạm mic | Item + báo thức nổ + popup | không dừng 60 s; cắt >500 ký tự; fullScreenIntent chưa kiểm |
| Copy → Kho | copy ở app khác | Item clip + undo | cần READ_LOGS; lưu cả mật khẩu |
| Kho → Drive → máy mới | bật sao lưu | dữ liệu khôi phục đủ | ghi đè; restore thiếu cột/nhắc |
| Recap | mở tab Recap/Thống kê | đọc tổng kết | AI tóm tắt chưa có |

## 4. Jev xếp hạng (26 lỗi, query: mất dữ liệu > hỏng luồng chính > treo/crash)
Điểm Jev = xác suất ý kiến, **chưa đo trên dữ liệu thật**. Cột "Điều chỉnh" là phán đoán của tôi sau khi tự kiểm code.
| Hạng Jev | Lỗi | Jev | Điều chỉnh |
|---|---|---|---|
| 1 | S1 Drive ghi đè bản sao lưu | 0.93 | giữ |
| 2 | S20 lost update `copy()` snapshot cũ | 0.93 | giữ |
| 3 | S7 xoá vĩnh viễn không xác nhận | 0.90 | giữ, sửa rẻ nhất |
| 4 | S10 Drive không timeout/401 | 0.89 | giữ |
| 5 | S11 không test/schema migration | 0.89 | giữ |
| 6 | S2 restore thiếu cột, mất nhắc | 0.88 | giữ |
| 7 | S19 decode ảnh trên main | 0.83 | giữ |
| 8 | S3 AI cắt 500 ký tự | 0.79 | **nâng lên top 3**: đã xác nhận, mất nội dung ghi âm dài |
| 9 | S13 fullScreenIntent | 0.76 | giữ (cần máy) |
| 10 | S15 parseWhen nhầm | 0.76 | giữ |
| 20 | S12 SHA-1 Drive release | 0.57 | **nâng lên top 5**: chặn hoàn toàn Drive trên release |
| 21 | S4 bubble lọt ảnh | 0.56 | **nâng**: đã xác nhận, hỏng luồng chính (chụp) |
| 23 | S9 purge định kỳ | 0.49 | giữ thấp |
| 24–26 | S22 vùng chạm · S21 FTS · S26 test | ≤0.40 | giữ thấp |

## 5. Khuyến nghị chiến lược (thứ tự làm)
| # | Việc | Lý do | Cỡ |
|---|---|---|---|
| 1 | Sửa 3 lỗi đã xác nhận trong luồng chính: bubble lọt ảnh (S4), cắt 500 ký tự (S3), xác nhận xoá vĩnh viễn (S7) | rẻ, chạm luồng dùng hằng ngày | nhỏ |
| 2 | Drive an toàn: gộp thay vì ghi đè + timeout + SHA-1 release (S1, S10, S12, S2) | rủi ro mất dữ liệu duy nhất không thể hoàn tác | vừa |
| 3 | Chống lost update: đổi `update(copy)` sang UPDATE theo cột (S20) | sửa tay bị AI ghi đè | vừa |
| 4 | Ghi âm: dừng ở 60 s, kiểm `AudioRecord`, dừng khi `onStop` (S5, S16) | crash/kẹt UI | nhỏ |
| 5 | Bật `exportSchema` + 1 test migration (S11) | trước khi đổi schema lần nữa | vừa |
| 6 | Hoãn: UI vùng chạm, FTS title/summary, host saveable, taskAffinity | không mất dữ liệu | — |
| Dừng | Phase 9 modular, polish UI thêm | lệch mục tiêu, xem báo cáo chiến lược trước | — |

## 6. Giới hạn của báo cáo
- Mọi trạng thái chỉ dựa vào code; chưa chạy app, chưa có logcat.
- Số dòng trong báo cáo agent có chỗ ước lượng (`~`/`≈`); 4 điểm ở mục 1 đã đối chiếu thật.
- Chưa kiểm: hiệu năng sheet, HyperOS giết nền, quyền sau `install -r`, `fullScreenIntent` trên Android 16.
- Chưa sửa dòng code nào.
