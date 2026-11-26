# 万汇网性能验收记录

| 项目 | 内容 |
|---|---|
| 文档编号 | ACC-PERF-001 |
| 版本 | 1.0 |
| 状态 | 已通过 |
| 执行日期 | 2026-11-27（北京时间） |
| 对应任务 | Task 28 / T-QA-004 |
| 覆盖验收项 | AC-NFR-001、AC-NFR-002 |
| 代码基线 | `main`，Task 27 完成后的提交 |
| 结论 | API 与首页性能均达到验收阈值 |

## 1. 验收口径

| 编号 | 场景 | 通过条件 | 结果 |
|---|---|---|---|
| AC-NFR-001 | 202 个资源；20 Mbps 下行、5 Mbps 上行、50 ms 延迟，清除缓存后首次打开资源首页 | 首屏 3 秒内完成渲染，无阻断性 JS 异常 | 通过 |
| AC-NFR-002 | 5,000 帖子、20,000 评论；20 并发持续请求 5 分钟 | P95 ≤ 2 秒，错误率 < 1% | 通过 |

## 2. 测试环境

| 项目 | 配置 |
|---|---|
| 操作系统 | Windows 11 家庭中文版 10.0.22631，64 位 |
| CPU | AMD Ryzen 7 8845H，8 核 16 线程 |
| 内存 | 27.8 GiB |
| Java | OpenJDK 21.0.13.1 LTS（Microsoft） |
| Spring Boot | 4.2.0 |
| 数据库 | MySQL 8.0 Docker，容器 `wanhui-mysql-dev`，健康状态 |
| Flyway | V1-V7 |
| 压测工具 | k6 v0.55.0（windows/amd64） |
| 浏览器 | Chromium 159.0.8154.6（Playwright） |
| 首页运行方式 | 前端生产构建，Docker Nginx 静态托管并反向代理 `/api` |
| API 直连 | `http://127.0.0.1:8080` |

## 3. 基线数据

| 数据 | 数量 | 来源 |
|---|---:|---|
| 资源 | 202 | `backend/src/main/resources/catalog/resources.json`，版本化只读 JSON，不入库 |
| 帖子 | 5,000 | `backend/src/test/resources/performance/seed.sql` |
| 评论 | 20,000 | 同上，均匀分布到 5,000 个帖子 |
| 基线用户 | 1 | `perf_seed_user` |

执行后可重复校验：

```sql
SELECT
  (SELECT COUNT(*) FROM posts WHERE title LIKE 'PERF-%') AS perf_posts,
  (SELECT COUNT(*) FROM post_comments WHERE content LIKE 'PERF-COMMENT-%') AS perf_comments;
```

本次结果：`perf_posts = 5000`，`perf_comments = 20000`。

## 4. API 压测

### 4.1 场景

`backend/src/test/resources/performance/k6-baseline.js` 使用 `constant-vus`：

| 参数 | 值 |
|---|---|
| 并发虚拟用户 | 20 VU |
| 持续时间 | 5 分钟 |
| 每轮请求 | 资源列表、帖子列表、论坛搜索、随机帖子详情、评论列表 |
| 请求间隔 | 200 ms |
| P95 阈值 | `< 2000 ms` |
| 错误率阈值 | `< 1%` |

### 4.2 实测结果

| 指标 | 结果 | 阈值 | 结论 |
|---|---:|---:|---|
| 完成迭代 | 8,188 | - | - |
| HTTP 请求总数 | 40,940 | - | - |
| 请求吞吐 | 136.21 req/s | - | - |
| P95 响应时间 | 299.33 ms | ≤ 2,000 ms | 通过 |
| P99 响应时间 | 344.05 ms | 无 | - |
| 平均响应时间 | 106.48 ms | 无 | - |
| 最大响应时间 | 578.16 ms | 无 | - |
| 错误率 | 0.00% | < 1% | 通过 |
| checks 通过率 | 100.00%（40,940 / 40,940） | 100% | 通过 |

5 个请求检查项全部通过：

```text
✓ resources 200
✓ posts 200
✓ search 200
✓ detail 200
✓ comments 200
```

结论：AC-NFR-002 通过。P95 约占阈值的 15%，无请求失败或超时。

## 5. 首页首屏

### 5.1 测量方法

- 使用 `npm run build` 生成生产构建。
- 使用本机 Docker Nginx 托管 `frontend/dist`，`/api` 反向代理到 Spring Boot。
- 每轮创建全新浏览器上下文，通过 Chrome DevTools Protocol 设置：
  - 下行 20 Mbps；
  - 上行 5 Mbps；
  - 往返延迟 50 ms；
  - 禁用缓存并清理浏览器缓存。
- 记录 FCP、LCP、DOMContentLoaded、资源 API 返回和首个资源卡片可见时间。
- 收集 `pageerror` 与失败请求，判断是否存在阻断性 JS 异常。

### 5.2 实测结果（Nginx 生产构建）

| 轮次 | LCP / FCP | 首个资源卡片可见 | JS 异常 | 失败请求 |
|---:|---:|---:|---:|---:|
| 1 | 1,036 ms | 1,185 ms | 0 | 0 |
| 2 | 1,112 ms | 1,189 ms | 0 | 0 |
| 3 | 1,032 ms | 1,174 ms | 0 | 0 |
| 4 | 1,060 ms | 1,200 ms | 0 | 0 |
| 5 | 1,008 ms | 1,137 ms | 0 | 0 |

范围：LCP/FCP **1,008-1,112 ms**，首个资源卡片 **1,137-1,200 ms**。结论：AC-NFR-001 通过。

补充说明：4 次运行出现非阻断的外部图标响应策略控制台提示；资源卡片已回退为首字母图标，页面渲染、路由和数据请求均正常，`pageerror` 为 0。该项列为非阻断图标优化项，不影响首屏 3 秒门禁。

### 5.3 Vite 生产预览复核

使用仓库内可重复脚本 `frontend/scripts/measure-home-performance.mjs` 对 `vite preview` 做辅助复核，5 次 LCP 为 **540-580 ms**，中位数 **576 ms**，JS 异常和失败请求均为 0。该结果用于验证脚本可执行性，正式判断采用更保守的 Nginx 结果。

## 6. 资源与优化项

| 项目 | 当前状态 | 影响 | 后续动作 |
|---|---|---|---|
| Element Plus 全量引入 | 主 JS 1,137.67 kB，gzip 367.82 kB；构建提示超过 500 kB | 当前限速下满足首屏要求，但移动网络余量有限 | Phase 3 按需引入、路由拆包和依赖分析 |
| 外部/相对图标地址 | 部分旧资源图标受上游响应策略影响 | 卡片显示首字母回退，非阻断 | 统一下载并托管图标，或使用受控图标代理 |
| API 搜索 | MySQL `LIKE` 搜索满足当前 5,000 帖子基线 | 数据增长后延迟可能上升 | 数据规模显著增长时引入专用全文检索 |

以上均为非阻断优化项，不改变本次 AC-NFR-001、AC-NFR-002 的通过结论。

## 7. 复现命令

### 7.1 准备数据库

```bash
docker compose -f deploy/compose.dev.yml up -d --wait
docker exec -i wanhui-mysql-dev mysql -uroot -plocal_root_password wanhui_dev \
  < backend/src/test/resources/performance/seed.sql
```

### 7.2 启动后端

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location backend
.\mvnw.cmd -B -s ..\maven-settings.xml spring-boot:run
```

### 7.3 运行 API 基线

```powershell
$k6 = 'C:\path\to\k6.exe'
& $k6 run `
  --summary-export .\k6-summary.json `
  -e BASE_URL=http://127.0.0.1:8080 `
  -e VUS=20 `
  -e DURATION=5m `
  .\backend\src\test\resources\performance\k6-baseline.js
```

通过条件：命令退出码为 0，P95 小于 2,000 ms，错误率小于 1%。

### 7.4 运行首页性能

终端 1：

```powershell
Set-Location frontend
npm ci
npm run build
npm run preview
```

终端 2：

```powershell
Set-Location frontend
$env:PERF_URL = 'http://127.0.0.1:4173/'
$env:PERF_RUNS = '5'
$env:PERF_OUTPUT = '.\home-performance.json'
npm run test:perf:home
```

脚本在 LCP 超过 3,000 ms、出现 JS 异常或失败请求时返回非零退出码。

## 8. 结论

- AC-NFR-001：通过。生产构建首屏 LCP 最大 1,112 ms，低于 3,000 ms。
- AC-NFR-002：通过。20 VU 持续 5 分钟，P95 299.33 ms，错误率 0%。
- 基线数据、k6 脚本和首页测量脚本均可重复执行。
- 未达标整改任务：无；存在 3 项非阻断性能优化项，已在第 6 节登记。