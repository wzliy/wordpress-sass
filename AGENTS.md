Read `docs/` before implementing anything.

## Primary Rule
- `docs/plan.md` is the single source of truth for:
  - current state
  - current recommended next task
  - task priority
  - completion log
- If the user says "按计划继续" or "执行下一个任务", read `docs/plan.md` first and continue from `Current Recommended Next Task` unless the user explicitly changes direction.

## Mandatory Bootstrap
Every time you newly take over this project or start a new task, follow this order:

1. Read `docs/plan.md`
- First read `Working Rules`
- Then read the milestone containing the target task
- Then read `Current Recommended Next Task`
- Then read the latest entries in `Change Log`

2. Read only the docs directly related to the task
- Architecture baseline: `docs/architecture.md`
- Product scope: `docs/requirement-breakdown.md`
- Refactor and domain boundaries: `docs/refactor-baseline.md`
- Site workspace: `docs/site-workspace-design.md`
- Async task model: `docs/async-task-design.md`
- Audit model: `docs/audit-log-design.md`
- Tenant isolation: `docs/tenant-isolation-audit.md`
- API contract: `docs/api.md`
- UI direction: `docs/ui-design-proposal.md`
- WordPress Multisite integration: `docs/wordpress-multisite.md`

3. Locate the matching code area before editing
- Backend module first
- Then frontend page/router/api
- Then the nearest tests

4. Validate after changes
- Backend changes: run `./gradlew test` or the narrowest relevant test class
- Frontend changes: run `npm run build` in `frontend`
- If the change is design-only, update `docs/plan.md` and skip code validation only if no executable artifact changed

5. Close the task properly
- Update task status in `docs/plan.md`
- Add the date and concrete completion note
- If you introduced a new module boundary, endpoint, table, or page, write or update the corresponding doc in `docs/`

## Task Selection Rules
- Default to the highest-priority unfinished task with the fewest unmet dependencies.
- Respect milestone ordering unless `docs/plan.md` explicitly says otherwise.
- Do not skip forward to later SaaS modules if an earlier P0 design or foundation task is still open.
- If a task depends on missing schema, contract, or design, finish that prerequisite first and update the plan.
- If blocked by an external dependency, mark the task `BLOCKED` in `docs/plan.md` with the reason.

## Continuous Execution Rule
- If the user asks to continue by plan, keep executing tasks in `docs/plan.md` sequentially without asking for confirmation after each task.
- Use `Current Recommended Next Task` first when it exists.
- After finishing a task:
  - update `docs/plan.md`
  - validate the change
  - immediately move to the next planned task
- Only stop and ask the user if:
  - a product or business decision is required
  - an external dependency blocks progress
  - the next action is destructive or high-risk
  - the plan or docs contain a conflict that must be resolved first
- Otherwise, do not pause between planned tasks.

## Current Project Snapshot
This repository is no longer centered on "content publish to WordPress".

Current product direction:
- Build a cross-border ecommerce site-building SaaS
- Mainline: site workspace, layout editor, payment, orders, cloak, ERP, async task center
- Legacy-compatible only: post and publish modules

Current status from the codebase:
- Backend stack: Java 17, Spring Boot 3.3, MyBatis, JWT auth, MySQL, H2 tests
- Frontend stack: Vue 3, Vite, Vue Router
- WordPress provision path exists through `wordpress-plugin/wpss-multisite-provisioner`
- `task/domain` and `ops/domain` currently contain model skeletons, not full application services yet
- Frontend navigation is still largely old-style (`dashboard / site / post / publish / user`), so current mainline tasks will reshape routes and menus

## Project Map

### Backend
- App entry: `src/main/java/com/wpss/wordpresssass/WordpressSassApplication.java`
- Config: `src/main/resources/application.yml`
- Common infra: `src/main/java/com/wpss/wordpresssass/common`
- Auth and tenant: `src/main/java/com/wpss/wordpresssass/auth`
- User module: `src/main/java/com/wpss/wordpresssass/user`
- Site module: `src/main/java/com/wpss/wordpresssass/site`
- Legacy post module: `src/main/java/com/wpss/wordpresssass/post`
- Legacy publish module: `src/main/java/com/wpss/wordpresssass/publish`
- Async task domain skeleton: `src/main/java/com/wpss/wordpresssass/task/domain`
- Audit domain skeleton: `src/main/java/com/wpss/wordpresssass/ops/domain`

### Frontend
- App shell: `frontend/src/App.vue`
- Global state/composition root: `frontend/src/composables/useConsoleApp.js`
- Router: `frontend/src/router/index.js`
- Navigation config: `frontend/src/config/navigation.js`
- API layer: `frontend/src/api`
- Pages: `frontend/src/pages`
- Shared layout/components: `frontend/src/components`
- Global styles: `frontend/src/style.css`

### Database and Docs
- Base schema: `docs/database.sql`
- Incremental migrations: `docs/migrations`
- Admin bootstrap SQL: `docs/init-admin.sql`
- UI references: `docs/resource`

### Tests
- Auth helpers: `src/test/java/com/wpss/wordpresssass/AuthTestSupport.java`
- Auth tests: `src/test/java/com/wpss/wordpresssass/auth`
- Site tests: `src/test/java/com/wpss/wordpresssass/site`
- User tests: `src/test/java/com/wpss/wordpresssass/user`
- Post tests: `src/test/java/com/wpss/wordpresssass/post`
- Publish tests: `src/test/java/com/wpss/wordpresssass/publish`
- SQL tenant isolation regression: `src/test/java/com/wpss/wordpresssass/common/TenantIsolationMapperTest.java`

## Task-to-Code Routing

### If the task is auth or tenant related
Read:
- `docs/api.md`
- `docs/plan.md`

Touch first:
- `src/main/java/com/wpss/wordpresssass/auth`
- `src/main/java/com/wpss/wordpresssass/common/auth`
- `src/main/java/com/wpss/wordpresssass/common/tenant`
- `src/test/java/com/wpss/wordpresssass/auth`

### If the task is site/workspace/template related
Read:
- `docs/site-workspace-design.md`
- `docs/architecture.md`
- `docs/refactor-baseline.md`
- `docs/wordpress-multisite.md`
- `docs/ui-design-proposal.md`

Touch first:
- `src/main/java/com/wpss/wordpresssass/site`
- `frontend/src/pages/site`
- `frontend/src/api/site.js`
- `frontend/src/router/index.js`
- `frontend/src/config/navigation.js`
- `src/test/java/com/wpss/wordpresssass/site`

### If the task is page/layout/editor related
Read:
- `docs/architecture.md`
- `docs/requirement-breakdown.md`
- `docs/refactor-baseline.md`
- `docs/ui-design-proposal.md`

Expect to add or extend:
- new backend modules adjacent to `site`
- new frontend pages and editor-specific components
- new migration scripts in `docs/migrations`

### If the task is async task / audit related
Read:
- `docs/async-task-design.md`
- `docs/audit-log-design.md`
- `docs/tenant-isolation-audit.md`

Touch first:
- `src/main/java/com/wpss/wordpresssass/task`
- `src/main/java/com/wpss/wordpresssass/ops`
- related module application services
- migration files and tests

### If the task is legacy post/publish compatibility related
Read:
- `docs/api.md`
- `docs/refactor-baseline.md`

Touch first:
- `src/main/java/com/wpss/wordpresssass/post`
- `src/main/java/com/wpss/wordpresssass/publish`
- `frontend/src/pages/post`
- `frontend/src/pages/publish`

Rule:
- keep compatibility, but do not expand legacy modules into new SaaS mainline responsibilities

## Implementation Rules For This Repo
- Preserve tenant isolation on every new business query and update; prefer `id + tenant_id` conditions.
- New SaaS mainline endpoints should use independent resources instead of overloading legacy post/publish resources.
- Do not use `post` or `post_publish` to carry new workspace, payment, ERP, or cloak data.
- Site-related UX should converge on the workspace model, not continue to grow around the plain site list page.
- When a task is design-first, land the design doc before code.
- When frontend navigation changes, update both router definitions and `frontend/src/config/navigation.js`.
- When adding backend DTOs or endpoints, update `docs/api.md` if the contract becomes user-facing.

## Validation Commands
- Backend full test: `./gradlew test`
- Single test example: `./gradlew test --tests com.wpss.wordpresssass.site.SiteControllerTest`
- Frontend build: `cd frontend && npm run build`

## Current Reentry Heuristic
When resuming after a pause, do not scan the whole repo blindly. Use this exact sequence:

1. `docs/plan.md`
2. task-specific design doc
3. matching backend package
4. matching frontend route/page/api
5. nearest test file

This is the fastest path back to productive work in this repository.
