# EasySpot Hub (bản xem trước)

Hub = trạm trung chuyển: chọn công ty/tòa nhà → mở app. Tĩnh thuần (HTML/CSS/JS), không backend, không build.
Dữ liệu mẫu, lưu `localStorage`, đồng bộ giữa các vai trò qua `BroadcastChannel`.

## Chạy

| Việc | Lệnh |
|---|---|
| Xem | phục vụ thư mục qua HTTP (`python3 -m http.server`) rồi mở `preview.html` |
| Kiểm thử | `node tests/smoke.js` (cần Playwright + Chromium) |
| Gộp 1 tệp | `node tools/build-standalone.js` (chạy lại sau mỗi lần sửa `client.html` / `shared/`) |

Mở bằng `file://` vẫn hiển thị, nhưng đồng bộ giữa các khung phụ thuộc trình duyệt → dùng HTTP.

## Đăng nhập (mẫu)

| Username | Mật khẩu | Vào |
|---|---|---|
| `client` | `1234` | Hub account admin (`client.html`) |
| `staff` | `1234` | Hub nhân viên (`staff.html`) |
| `admin` | `1234` | Console EasySpot (`admin.html`) |

- Một cửa cho mọi người: tài khoản quyết định workspace mở ra. Mở thẳng trang vai trò khi chưa đăng nhập → bị đưa về `index.html#login`.
- Bản mẫu thiết kế: tài khoản nằm ngay trong `shared/auth.js`, ai xem mã nguồn cũng thấy. Không lưu mật khẩu. Bản thật thay `authenticate()` bằng dịch vụ định danh.
- Sign up dừng ở "Check your email" (không gửi thư); đăng nhập bằng email đó → nhắc xác minh.
- `preview.html` (xem 3 vai trò cùng lúc) tự đăng nhập 3 người mẫu.
- "Preview options" (đổi dữ liệu mẫu, giả lỗi) nằm trong menu tài khoản, không còn thanh vàng.

## Tệp

| Tệp | Vai trò |
|---|---|
| `shared/registry.js` | **App Registry** + bộ từ vựng trạng thái + `resolveAppState()` + cấu hình hệ thống |
| `shared/hub-view.js` | **Hub thuần**: lưới app, ghim/gần đây, lọc, `launchApp()` |
| `shared/contract.js` | **Hub Context Contract v1**: tệp duy nhất app con cần |
| `shared/settings.js` | Cài đặt cá nhân + khung trang Settings |
| `index.html` | **Cửa trước**: trang ngoài, Log in, Sign up, quên mật khẩu (`#login · #signup · #forgot`) |
| `shared/auth.js` | Tài khoản mẫu + kiểm đăng nhập, khóa tạm sau 5 lần sai, đăng ký chờ xác minh |
| `tools/build-standalone.js` → `dist/easyspot-applicator.html` | Gộp cửa trước + 3 workspace thành **1 tệp** mở ở đâu cũng được |
| `shared/chrome.js` | Khung vỏ: sidebar, topbar, tìm kiếm, modal, kho đồng bộ, thông báo |
| `shared/hub.css` | Token + style dùng chung (Hub và app con) |
| `client.html` | Hub của account admin + khu quản trị (App access, Staff, Organization) |
| `staff.html` | Hub của nhân viên |
| `admin.html` | Console nội bộ EasySpot (Clients, System) — không có Hub |
| `apps/ops.html` | App con **mẫu** (phía nhận contract). Không đăng ký trong Registry; thẻ EasySpotOps mở `client.` / `staff.easyspotops.com` theo vai trò |
| `preview.html` | Khung xem 3 vai trò |

Ranh giới: `hub-view.js` chỉ hiển thị + mở app. Mọi thao tác quản trị nằm ở trang vai trò, dưới nhóm "Account admin" hoặc Settings.

## Thêm app mới

1. Thêm 1 mục vào `APP_REGISTRY` (`shared/registry.js`).
2. Thêm màu `.ic-<id>` trong `hub.css`.
3. App có đích thật: `launch:{type:'url',url,target:'self'|'blank',params}` + `lifecycle:'live'|'beta'`. Chưa có: `launch:{type:'modal_preview'}` + `coming_soon`.

| Trường | Ý nghĩa |
|---|---|
| `id, slug, name, shortName, category, description, icon, color, keywords, highlights` | nhận diện + tìm kiếm |
| `lifecycle` | `live · beta · coming_soon · maintenance` (admin ghi đè toàn hệ thống trong Settings → System) |
| `launch` | cách mở + tham số ngữ cảnh truyền đi; `urlByRole` = cửa vào riêng theo vai trò |
| `requiresSingleProperty` | đang "All buildings" → hỏi chọn tòa nhà trước khi mở |
| `allowedRoles` | `null` = mọi vai trò |

## Trạng thái (một bộ cho mọi góc nhìn)

| Loại | Giá trị |
|---|---|
| App | `open · new · pending · available · request_access · locked` |
| Yêu cầu | `pending · approved · denied · expired` |
| Vòng đời | `live · beta · coming_soon · maintenance` |

`resolveAppState(id, ctx)` → `{state, reason, lifecycle, ready, launchable, req}`. `reason` quyết định câu giải thích + bước tiếp theo.

## launchApp — thứ tự kiểm tra

| # | Điều kiện | Kết quả |
|---|---|---|
| 1 | hết phiên | màn đăng nhập lại |
| 2 | `available` / `request_access` | vào thẳng luồng thêm app / xin quyền |
| 3 | `pending` / `locked` / chưa `launchable` | hộp thoại: lý do + bước tiếp (duyệt, hỏi EasySpot, báo khi sẵn sàng) |
| 4 | ngoại tuyến | báo lỗi + Try again |
| 5 | cần 1 tòa nhà, đang "All buildings" | chọn nhanh tòa nhà (Hub đổi theo) |
| 6 | đạt | ghi Recent, lưu trạng thái Hub, mở URL kèm ngữ cảnh |

## Hub Context Contract v1

Hub → app (query string): `hub=1` + `app, companyId, company, propertyId, propertyKey, property, role, userId, user, theme, locale, dateFormat, timeZone, returnUrl`.
Bắt buộc: `app, companyId, role, userId, returnUrl`; app tự khai thêm (`require`).

| Phía | Hàm |
|---|---|
| Hub | `HubContract.build(launch, ctx)` · `parseReturn(hash)` |
| App | `read(search, {app, require})` → `{ok, ctx, errors}` · `backUrl(ctx, info)` · `notifyHub(ctx, {type, text})` · `signedOut(userId)` |

- App quay về: `returnUrl#hub-return=<json>`; Hub xóa hash, báo, trả focus về thẻ app.
- `returnUrl` phải cùng origin, chỉ `http(s)`/`file` → không chuyển hướng ra ngoài. App ở tên miền khác: khai `hubOrigins:['https://<hub>']` trong `read()` / `backUrl()`.
- Thiếu/sai ngữ cảnh → app từ chối, không đoán công ty/tòa nhà.
- Trạng thái Hub (view, bộ lọc, tòa nhà) giữ trong `sessionStorage` → quay lại như cũ.

## Kho dữ liệu

| Khóa | Ghi | Nội dung |
|---|---|---|
| `ea:cli_001:apps` | admin | app EasySpot bật cho công ty |
| `ea:sys:config` | admin | vòng đời app toàn hệ thống, hạn yêu cầu bật app |
| `ea:admin:enable:cli_001` | admin | hộp thư yêu cầu bật app |
| `ea:cli_001:enable_out` | client | yêu cầu bật app gửi EasySpot |
| `ea:cli_001:ent` · `grants:<uid>` · `invites` | client | app của công ty, quyền theo tòa nhà, lời mời |
| `ea:cli_001:org` · `staff` | client | tòa nhà, chính sách yêu cầu, nhân sự |
| `ea:cli_001:requests:<uid>` | staff, client | yêu cầu quyền |
| `ea:notes:<topic>:<sender>` | người gửi | thông báo; topic = `admin · cli_001 · cli_001.admins · <uid> · all` |
| `ea-settings:<uid>` · `ea-web-theme` · `ea-session:<uid>` | cục bộ | cài đặt cá nhân, giao diện, đăng xuất toàn bộ |

Quyền đọc/ghi từng khóa: `STORE_READ / STORE_WRITE / STORE_FROM` ở đầu mỗi trang vai trò.

## Giới hạn đã biết

- Bản 1 tệp: 3 workspace nằm trong tệp, mở trong khung phủ toàn trang sau khi đăng nhập; client tự nạp dữ liệu mẫu lần đầu.
- Phông Inter + Space Grotesk tải từ Google Fonts; ngoại tuyến thì rơi về phông hệ thống.

- Bản xem trước: không xác thực thật; app con tin ngữ cảnh trên URL. Bản thật phải kiểm quyền phía máy chủ.
- Chỉ tiếng Anh; ngôn ngữ khác hiện "coming soon".
- Kênh Email trong Notifications chỉ lưu lựa chọn, không gửi.
- Hết phiên do không hoạt động chỉ áp cho Hub; app con chỉ theo "đăng xuất toàn bộ".
- Tòa nhà: thêm + đổi tên, chưa xóa.
- Admin "Reset sample data" không xóa yêu cầu client đã gửi (khóa của client) → hiện lại là pending.
- Chỉ `EasySpotOps` có đích thật: account admin → `https://client.easyspotops.com/`, admin nội bộ → `https://admin.easyspotops.com/` (nút ở sidebar console), vai trò khác → `https://staff.easyspotops.com/` (`launch.urlByRole`; tab mới vì trang cấm nhúng iframe); 3 app còn lại `coming_soon`.
- Chưa đăng nhập → trang Ops chuyển sang `auth.easyspotops.com/login?next=/`: tham số ngữ cảnh rơi mất ở bước này.
- `easyspotops.com` chưa đọc contract: tham số ngữ cảnh được gửi kèm nhưng trang bỏ qua, người dùng vẫn đăng nhập + chọn tòa nhà ở đó. Muốn liền mạch: trang Ops gọi `HubContract.read(..., {hubOrigins})`.
- Khác tên miền → chuông Hub (`notifyHub`) và "đăng xuất toàn bộ" không tới được trang Ops (đều dựa `localStorage`); cần backend/SSO.
