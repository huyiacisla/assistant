# ADR — Kiến trúc đích của Recap (Kotlin, 1 module)

Status: accepted 21-10-2026 · Phân loại: `02-architecture` (quyết định kiến trúc, không phải kế hoạch)
Gốc: [modular_structure](modular_structure_20-10-2026.md) (giữ nguyên) · Audit: [audit matrix](../10-reports/audit_screen_feature_workflow_matrix_21-10-2026.md)

## Quyết định
| # | Quyết định | Vì sao |
|---|---|---|
| 1 | **1 module Gradle `:app`, modular bằng package theo tính năng** (không đổi, đã chốt 20-10) | 4,3k dòng, 1 người dùng; xét lại khi > ~10k dòng hoặc build chậm |
| 2 | **Room là nguồn sự thật duy nhất**; UI chỉ quan sát `Flow` | tránh trạng thái song song |
| 3 | **Ghi theo cột**, không `update(item.copy())` bằng bản chụp cũ | lost update giữa AI, popup nhắc, sửa tay (audit S20) |
| 4 | Logic nghiệp vụ là **hàm thuần** (không import Android/Room) để test được | hiện chỉ test hàm thuần được |
| 5 | Lỗi là **giá trị** (`Outcome`) cho AI/Drive thay vì `null` | phân biệt "không khoá" / "lỗi mạng" / "quá dài" |
| 6 | Giờ truyền vào (`Clock`) cho logic remind/purge/parse | test không phụ thuộc giờ máy |
| 7 | DI thủ công (`AppGraph`), không Hilt | 5–6 đối tượng |
| 8 | `exportSchema=true`, schema JSON commit trong `app/schemas` | đối chiếu khi đổi version Room |

## Luật phụ thuộc (một chiều)
| Từ | Được gọi | Không được gọi |
|---|---|---|
| `feature/*/*.screen.kt` | state holder, `shared/ui` | DAO, mạng |
| state/logic của feature | `store`, `core`, client mạng | Composable |
| `core`, `store`, `shared` | stdlib, nhau theo chiều xuống | `feature`, `overlay`, `app` (đã kiểm phase 9) |
| `overlay`, service, receiver | `store`/repo, logic | DAO ghi nguyên `Item` |

## Không làm (YAGNI)
Module Gradle · Hilt/Koin · UseCase cho mỗi hàm · mapper entity↔model↔UI · KMP.

## Thước 100 điểm và điểm hiện tại (ước tính từ code, 21-10)
| Hạng mục | Tối đa | Trước | Sau phase 14 |
|---|---|---|---|
| Dữ liệu | 20 | 11 | 14 (schema export, ghi theo cột; còn Drive gộp, test DAO) |
| Ranh giới | 15 | 8 | 8 |
| Trạng thái UI | 15 | 6 | 6 |
| Lỗi & độ bền | 15 | 6 | 6 |
| Test | 15 | 4 | 5 |
| Build & phát hành | 10 | 6 | 7 |
| Quy ước & tài liệu | 10 | 7 | 8 |
| **Tổng** | **100** | **≈48** | **≈54** |

Lộ trình điểm tiếp theo: test DAO + migration → `Outcome`/timeout → Drive gộp → state holder → `Clock` → build gate. Chi tiết ở [phase 14](../04-phases/phase_14_data_safety_foundation_21-10-2026.md).
