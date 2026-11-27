# 万汇网响应式界面修复实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复桌面无侧栏页面宽度、移动端主导航和移动侧栏首屏占用问题。

**Architecture:** 通过 `AppShell` 的动态布局类修复桌面主体宽度，通过 CSS 媒体查询把主导航和分类导航改为移动端横向布局，不改变现有页面结构和 API。

**Tech Stack:** Vue 3、TypeScript、Vitest、Playwright、Vite。

**Spec:** `docs/superpowers/specs/2026-11-27-responsive-ui-design.md`

## Global Constraints

- 保持现有 Vue 3 + TypeScript + Vite 技术栈，不新增依赖。
- 桌面验收宽度 1440px，移动验收宽度 375px。
- 页面不得出现横向滚动。
- 不改动后端 API、数据库和权限行为。

## Review Focus

- 无侧栏页面必须全宽：登录、注册、帖子详情、关于、隐私、条款、404、管理页。
- 移动端主导航必须始终可用，登录后仍能进入个人中心和管理页。
- 移动端侧栏不能独占首屏，分类导航必须可横向滚动。
- 原有桌面首页、论坛和用户中心布局不能回归。
- 现有 E2E 注册、登录、发帖、收藏流程不能受影响。

---

### Task 1: 修复 AppShell 无侧栏布局

**Files:**
- Modify: `frontend/src/layouts/AppShell.vue`
- Test: `frontend/src/layouts/AppShell.spec.ts`
- Test: `frontend/e2e/layout.spec.ts`

**Interfaces:**
- Produces: `.app-body--with-sidebar` 类仅在存在侧栏插槽时出现。
- Produces: `.app-body--full` 类在无侧栏时出现。

- [ ] 写 AppShell 单元测试，验证无侧栏和带侧栏两种 class。
- [ ] 运行定向单元测试，确认 RED。
- [ ] 写 Playwright 桌面登录页宽度测试，确认当前 RED。
- [ ] 在 `AppShell.vue` 中动态绑定布局 class，并调整网格 CSS。
- [ ] 重跑单元测试与桌面布局 E2E，确认 GREEN。

### Task 2: 修复移动端导航与侧栏

**Files:**
- Modify: `frontend/src/components/AppHeader.vue`
- Modify: `frontend/src/components/AppSidebar.vue`
- Modify: `frontend/src/layouts/UserLayout.vue`
- Modify: `frontend/src/pages/ForumPage.vue`
- Test: `frontend/e2e/layout.spec.ts`

**Interfaces:**
- Preserves: `aria-label="主导航"`、`aria-label="资源分类"`、`aria-label="个人中心导航"`，供 E2E 定位。

- [ ] 写移动端主导航可见性测试，确认当前 RED。
- [ ] 写资源分类移动端横向排列测试，确认当前 RED。
- [ ] 修改 `AppHeader`，移动端显示第二行横向导航。
- [ ] 修改资源、论坛和用户中心侧栏，移动端使用横向滚动标签。
- [ ] 重跑布局 E2E，确认 GREEN。

### Task 3: 文案、数据与完整回归

**Files:**
- Modify: `frontend/src/pages/RegisterPage.vue`
- Modify: `frontend/e2e/layout.spec.ts`
- Verify: `frontend/src/**/*.spec.ts`
- Verify: `frontend/e2e/**/*.spec.ts`

**Interfaces:**
- Produces: 注册用户名占位文案与实际允许字符一致。

- [ ] 写注册页占位文案测试，确认当前 RED。
- [ ] 修改注册页文案。
- [ ] 清理本地开发库中的 PERF 与 E2E 演示数据。
- [ ] 运行全量单元测试、生产构建和全量 E2E。
- [ ] 用桌面与移动截图复核登录、主页、论坛、个人资料、帖子详情和关于页。
- [ ] 提交并合并回 `main`。