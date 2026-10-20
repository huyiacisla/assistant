# Build & chạy máy thật (chi tiết kỹ thuật)

Doc chi tiết cho bước 6 của [vibecoding_workflow](vibecoding_workflow_20-10-2026.md). Máy: xem `AGENTS.md` mục 6.

## Lệnh
| Việc | Lệnh |
|---|---|
| Build | `./gradlew :app:assembleDebug` |
| Cài | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| Mở app | `adb shell am start -W -n com.huyi.recap/.app.MainActivity` |
| Ảnh màn hình | `adb exec-out screencap -p > file.png` |
| Log | Logcat tag `Recap` |
| Nút nổi còn chạy? | `adb shell dumpsys activity services com.huyi.recap \| grep -c OverlayService` (3 = chạy, 0 = chết) |
| Tối/sáng | `adb shell cmd uimode night yes\|no` |

## Sau mỗi lần cài
- `install -r` giết `OverlayService` → bật lại bằng pill "Bật nút nổi" trong Kho (chờ ≥ 3s sau `am start -W`).
- Gỡ app = mất quyền `READ_LOGS` (cấp lại: `adb shell pm grant com.huyi.recap android.permission.READ_LOGS`) → hỏi Founder trước.
- Trợ năng (`ShotAccessibilityService`) có thể phải bật lại.

## Thao tác giả lập
- Nhấn giữ: `adb shell input swipe x y x y 1200`.
- Tọa độ thật = ảnh hiển thị × 1.39 (1280×2772); ảnh >2000px có thể không đọc được → chụp lại.
- Menu/bubble mở trễ: chờ ~1.5s trước khi chụp; kiểm tra service còn sống trước khi tap.
- Một máy chỉ cho **một** phiên điều khiển adb tại một thời điểm (nhiều session song song làm tap lệch, đóng bubble).

## Giới hạn đã biết
- `gfxinfo` không đếm khung overlay → không đo được fps nút nổi.
- Nhận dạng giọng (SpeechRecognizer) chưa chứng minh trên ROM CN.
