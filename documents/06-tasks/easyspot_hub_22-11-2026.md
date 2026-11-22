# EasySpot Hub — hoàn thiện trạm trung chuyển (4 giai đoạn)

Type: TASK · Status: done · Phân loại: `06-tasks` (chọn tay — Jev không có trong phiên) · Mã: [`easyspot-hub/`](../../easyspot-hub/README.md)
Ngoài scope Recap: prototype web riêng, đặt cùng repo theo quyết định Founder 22/11. Không đụng roadmap/phase của Recap.

## Input
| Trường | Giá trị |
|---|---|
| Yêu cầu | Bài "Hoàn thiện hệ thống Hub trạm trung chuyển EasySpot" + 4 tệp `preview/admin/client/staff.html` |
| Phạm vi chốt | Code cả 4 giai đoạn; thư mục riêng `easyspot-hub/` |
| Tiêu chí xong | Hub mở app qua Registry + `launchApp` đủ nhánh · Settings phân tầng áp ngay · trạng thái/thông báo đồng bộ 3 vai trò · app con mẫu nhận ngữ cảnh + quay về giữ trạng thái · test tự động xanh |

## Analysis
| Điểm | Chi tiết |
|---|---|
| Problem | CSS + khung vỏ lặp y hệt ở 3 tệp; mở app viết cứng trong từng tệp; trạng thái lệch tên giữa client/staff; thông báo chỉ nằm trong bộ nhớ từng khung |
| Assumption | Vẫn là bản xem trước tĩnh, không backend; dữ liệu mẫu |
| Rejected | Tách `hub.html` riêng khỏi trang vai trò — trạng thái trong bộ nhớ sẽ mất khi chuyển tệp; thay bằng tách module (`hub-view.js` chỉ launcher, quản trị ở khu riêng) |
| Rejected | Viết lại bằng framework/bundler — không cần cho 4 app, giữ mở trực tiếp được |

## Plan
| Check | ID | Action | Status |
|---|---|---|---|
| [x] | G1.1 | Tách `shared/hub.css` + `shared/chrome.js` khỏi 3 tệp | done |
| [x] | G1.2 | `shared/registry.js`: App Registry, từ vựng trạng thái, `resolveAppState` | done |
| [x] | G1.3 | `shared/hub-view.js`: lưới responsive, ghim/gần đây, lọc All/Ready/Pending, `launchApp` | done |
| [x] | G2.1 | `shared/settings.js`: giao diện + mặc định, ngôn ngữ + khu vực, thông báo, phiên | done |
| [x] | G2.2 | Organization (client): tòa nhà, hạn yêu cầu, quyền mặc định · System (admin): vòng đời app, hạn yêu cầu | done |
| [x] | G2.3 | Settings mở từ menu tài khoản + tìm kiếm toàn cục (app, chức năng, tòa nhà, mục cài đặt) | done |
| [x] | G3.1 | Một bộ trạng thái app/yêu cầu cho cả 3 vai trò | done |
| [x] | G3.2 | Thông báo qua kho chung `ea:notes:<topic>:<sender>`; chuông cập nhật chéo vai trò | done |
| [x] | G3.3 | Luồng client xin EasySpot bật app → admin duyệt → client nhận | done |
| [x] | G4.1 | `shared/contract.js`: Hub Context Contract v1 | done |
| [x] | G4.2 | `apps/ops.html`: app con mẫu, quay về Hub giữ trạng thái | done |
| [x] | G4.3 | `tests/smoke.js`: 58 kiểm tra (launch, yêu cầu, thông báo, cài đặt, phiên, 360px) | done |

## Execution Log
| Date | Actor | Change |
|---|---|---|
| 22-11-2026 | Claude | Làm G1→G4 liền một mạch; `node tests/smoke.js` → 58 passed, 0 failed |
| 22-11-2026 | Claude | Chặn ký tự đánh dấu trong tên tòa nhà (giờ là dữ liệu người dùng nhập, đồng bộ sang staff) |
| 22-11-2026 | Claude | Thẻ EasySpotOps trỏ `https://easyspotops.com/` (tab mới: trang cấm iframe); sửa lỗi `window.open` + `noopener` luôn báo bị chặn; contract nhận `hubOrigins` cho app khác tên miền; test 62 passed |
| 22-11-2026 | Claude | Ops mở theo vai trò: account admin → `client.easyspotops.com`, còn lại → `staff.easyspotops.com` (`launch.urlByRole`); test 63 passed |
| 22-11-2026 | Claude | Console admin thêm nút mở `admin.easyspotops.com`; mỗi trang vai trò về đúng cửa vào của mình; test 64 passed |
| 22-11-2026 | Claude | Trang ngoài (landing + đăng nhập) cho EasySpot Applicator; lớp hoàn thiện giao diện Hub (phông, thẻ app canh trái ở màn rộng); `dist/easyspot-applicator.html` 1 tệp độc lập; pop-up bị chặn → có link mở trực tiếp; test 72 passed |
| 22-11-2026 | Claude | Cửa trước thật: `index.html` (landing, Log in, Sign up, quên mật khẩu) + `shared/auth.js`; tài khoản `client` / `staff` / `admin`, mật khẩu `1234`; trang vai trò bắt buộc đăng nhập; bỏ thanh demo; bản 1 tệp chứa cả 3 workspace; test 87 passed |

## Retrospective
| Kết quả | Vấn đề | Bài học |
|---|---|---|
| 3 trang vai trò giảm từ ~830–960 dòng còn ~215–400; phần chung nằm 1 chỗ | Bước 6 workflow (máy thật Android) không áp dụng — thay bằng Chromium headless | Dữ liệu hằng thành dữ liệu nhập → phải soát lại mọi chỗ vẽ ra màn hình |
| Giới hạn còn lại: xem [README](../../easyspot-hub/README.md#giới-hạn-đã-biết) | Chưa quét semgrep/ast-grep (không có trong phiên) | — |
