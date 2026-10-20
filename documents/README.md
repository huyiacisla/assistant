
```json
{
  "lifecycle": "product/architecture → plan → phase → design → task/issue → QA → report → archive",
  "folders": {
    "typed (Schema 2, khuôn ở template-schema.json)": {
      "00-guides": "quy tắc đang hiệu lực và công cụ quản lý tài liệu",
      "01-product": "product doctrine, scope, module catalog, current state",
      "02-architecture": "system boundaries, data model, roles, interface contract",
      "03-plans": {
        "purpose": "roadmap, rollout, migration, implementation plan",
        "proposals/": "đề xuất cải tiến, đổi công nghệ chưa duyệt (Status: draft)"
      },
      "04-phases": "phase objective, deliverables, Definition of Done, retrospective",
      "05-designs": "UX direction, flow, wireframe, visual decisions",
      "06-tasks": "một đơn vị công việc thực thi độc lập",
      "07-issues": {
        "bugs/": "reproduction, root cause, fix, verification",
        "refactors/": "no-behavior-change refactor"
      },
      "08-qa": "test plan, checklist, evidence, acceptance, regression",
      "09-runbooks": "local setup, deployment, recovery, operational procedure",
      "10-reports": "audit, health check, analysis, retrospective",
      "13-backlog": "sổ việc tồn (nợ · việc chờ quyết định · việc chưa xếp lịch): `oos_debt` · `production_hardening_debt` · `tech_debt_ledger…` · `unfinished_docs_backlog`; 1 dòng = 1 mục có ID; bắt đầu làm ⇒ mở TASK/BUG/REFACTOR"
    },
    "untyped (không bắt buộc khuôn)": {
      "11-reference": "research, prompts, tips, external reference (tạo khi cần)",
      "12-json": {
        "purpose": "khuôn JSON máy đọc, phân cấp 12-json/<nhiệm-vụ>/<tên>.schema.json (+ <tên>.example.json khi cần); chỉ khuôn, dữ liệu sinh ra khi chạy nằm ngoài (vd .workflow/)",
        "workflow/": "run.schema.json: khuôn file run do pnpm flow run:new sinh"
      },
      "99-archive": "tài liệu superseded/không còn canonical; không phải source of truth (tạo khi cần)",
      "_analysis": "dump nguồn cho NotebookLM (tạo khi cần)",
      "_tech": "ghi chú kỹ thuật legacy, không canonical",
      "outputs": "output noncanonical và generated document index"
    },
    "meta": {
      "template-schema.json": "khuôn của 13 Type (folder · status · mục · bảng); docs:new dựng md từ đây, không có _template.md",
      "README.md": "hợp đồng meta của repo"
    }
  },
}
```
