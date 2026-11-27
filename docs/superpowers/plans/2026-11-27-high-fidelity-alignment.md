# 万汇网高保真视觉对齐实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** 按高保真原型重构万汇网前端视觉，同时保持现有业务功能。

**Architecture:** 先建立绿色品牌设计令牌和 Element Plus 主题，再依次重构全局壳、资源页、论坛/详情、认证和用户/管理页面；每个任务以计算样式和 E2E 行为测试验证。

**Tech Stack:** Vue 3、TypeScript、Vite、Element Plus、Vitest、Playwright。

**Spec:** `docs/superpowers/specs/2026-11-27-high-fidelity-alignment-design.md`

## Global Constraints

- 视觉基线为 `万汇网高保真原型.html`。
- 主色 `#176b5b`，品牌深色 `#115447`，标题 `#1b2522`，边框 `#dee5e0`。
- 桌面验收 1440×900，移动验收 375×667。
- 不修改后端 API、数据库和权限模型。
- 不引入新依赖。

## Review Focus

- 顶栏必须包含品牌、全局搜索、论坛和登录/用户入口。
- 资源页必须保持搜索、分类、分页和收藏行为。
- 论坛必须有深绿色主视觉和可读的单列帖子列表。
- 登录/注册必须为左右分栏，移动端退化为单列。
- 个人中心、管理页和错误状态不能因视觉重构失去可操作性。

---

### Task 1: 全局设计令牌与顶栏

**Files:**
- Modify: `frontend/src/style.css`
- Modify: `frontend/src/components/AppHeader.vue`
- Modify: `frontend/src/layouts/AppShell.vue`
- Test: `frontend/e2e/design.spec.ts`

- [x] 写 Playwright 测试，验证主色、顶栏品牌标识、搜索框和登录按钮。
- [x] 运行定向 E2E，确认 RED。
- [x] 添加绿色设计令牌、Element Plus 覆盖和字体/背景。
- [x] 重构 AppHeader 为品牌 + 全局搜索 + 论坛 + 账户入口。
- [x] 调整 AppShell 最大宽度、侧栏和内容间距。
- [x] 运行定向 E2E，确认 GREEN。

### Task 2: 资源首页与资源卡片

**Files:**
- Modify: `frontend/src/pages/ResourceHomePage.vue`
- Modify: `frontend/src/components/AppSidebar.vue`
- Modify: `frontend/src/components/ResourceCard.vue`
- Test: `frontend/src/pages/ResourceHomePage.spec.ts`
- Test: `frontend/e2e/design.spec.ts`

- [x] 写资源卡片主按钮、边框、圆角和首页网格测试，确认 RED。
- [x] 重构资源首页 Hero、工作台侧栏、分类和结果区。
- [x] 重构资源卡片图标、标签、访问按钮和收藏按钮。
- [x] 保留搜索、分页、收藏和空状态行为。
- [x] 运行单元测试和定向 E2E，确认 GREEN。

### Task 3: 论坛与帖子详情

**Files:**
- Modify: `frontend/src/pages/ForumPage.vue`
- Modify: `frontend/src/components/PostCard.vue`
- Modify: `frontend/src/pages/PostDetailPage.vue`
- Test: `frontend/src/pages/ForumPage.spec.ts`
- Test: `frontend/e2e/design.spec.ts`

- [x] 写论坛 Hero、帖子卡片和详情卡片的视觉测试，确认 RED。
- [x] 重构论坛深绿色 Hero、分类侧栏和帖子列表。
- [x] 重构帖子详情面包屑、作者行、互动区和评论区。
- [x] 保留发帖、搜索、分页、点赞、收藏和评论行为。
- [x] 运行定向测试，确认 GREEN。

### Task 4: 登录、注册、个人中心和管理页

**Files:**
- Modify: `frontend/src/pages/LoginPage.vue`
- Modify: `frontend/src/pages/RegisterPage.vue`
- Modify: `frontend/src/layouts/UserLayout.vue`
- Modify: `frontend/src/pages/UserProfilePage.vue`
- Modify: `frontend/src/pages/AdminContentPage.vue`
- Test: `frontend/src/pages/LoginPage.spec.ts`
- Test: `frontend/e2e/design.spec.ts`

- [x] 写登录分栏、用户侧栏和管理表格视觉测试，确认 RED。
- [x] 重构登录/注册为双栏布局并保留表单行为。
- [x] 调整个人中心侧栏、资料卡片和头像区域。
- [x] 调整管理页统计卡和表格样式。
- [x] 运行定向测试，确认 GREEN。

### Task 5: 全量回归与视觉验收

**Files:**
- Create: `docs/acceptance/high-fidelity-alignment.md`
- Modify: `docs/acceptance/README.md`

- [x] 运行 Vitest、生产构建和全量 E2E。
- [x] 用 1440×900 和 375×667 截图对照高保真原型。
- [x] 修复发现的关键视觉问题。
- [x] 清理本地 E2E 测试数据。
- [x] 提交分支并合并到 `main`。