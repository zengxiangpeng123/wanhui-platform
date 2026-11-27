# 万汇网验收证据索引

| 项目 | 内容 |
|---|---|
| 文档编号 | ACC-INDEX-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-27 |
| 适用基线 | 万汇网 MVP + Phase 2 |
| 总体结论 | 本地功能、安全、性能与部署配置验收通过；云端域名实链路待部署复核 |

## 1. 验收记录

| 验收域 | 记录 | 主要结果 |
|---|---|---|
| 核心用户流程 | [MVP E2E 记录](./2026-11-24-e2e-record.md) | 最终全量 28/28 通过 |
| 安全与合规 | [Phase 2 安全验收](./phase2-security.md) | 34 个安全用例通过；Cookie、密文、限流已实测 |
| 性能 | [性能验收](./performance.md) | API P95 299.33 ms、错误率 0%；首页 LCP 最大 1,112 ms |
| 响应式界面 | [响应式界面修复验收](./responsive-ui.md) | 桌面主体宽度、移动导航和横向侧栏通过 |
| 高保真视觉 | [高保真视觉对齐验收](./high-fidelity-alignment.md) | 深绿色品牌、顶栏、资源卡、论坛、认证和管理页完成对齐 |
| 部署 | [Docker staging 部署验收](./deployment.md) | 前后端镜像构建成功；三容器 healthy；HTTPS、V7、安全 Cookie 通过 |
| 部署与恢复 | [备份恢复与回滚演练](../operations/drill-record-2026-11-26.md) | 备份 SHA256 与逐表计数一致；本地回滚产物可启动 |
| 生产检查 | [生产发布检查表](../operations/production-checklist.md) | 发布前、部署后、回滚与安全门禁 |
| 发布检查 | [发布检查表](../operations/release-checklist.md) | staging 发布与回滚步骤 |
| 安全检查 | [上线前安全检查表](../../deploy/security-checklist.md) | 认证、CSRF、权限、加密、传输和部署配置 |

## 2. 最终回归快照

执行日期：2026-11-27（北京时间）

| 套件 | 结果 | 说明 |
|---|---|---|
| 后端 `mvnw clean test` | 96 通过，0 失败，1 跳过 | Testcontainers MySQL 从空库执行 Flyway V1-V7 |
| 前端 `npm ci` | 通过 | 安装完成，0 个已知漏洞 |
| 前端 `vitest run` | 47/47 通过 | 16 个测试文件 |
| 前端 `vite build` | 通过 | 1,137.67 kB 主 JS，gzip 367.82 kB；有待优化的大包警告 |
| Playwright E2E | 36/36 通过 | 桌面 18 + 移动 18，2.2 分钟 |
| API 性能 | 通过 | 20 VU × 5 分钟，40,940 请求，P95 299.33 ms |
| 首页性能 | 通过 | 20/5 Mbps、50 ms、禁用缓存，LCP 1,008-1,112 ms |
| 生产配置检查 | 通过 | `CONFIG CHECK PASSED` |
| Compose 语法 | 通过 | staging / prod 使用示例环境文件解析成功 |

## 3. 关键验收命令

```powershell
# 后端
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
Set-Location backend
.\mvnw.cmd -B -s ..\maven-settings.xml clean test

# 前端
Set-Location ..\frontend
npm ci
npm run test:unit:run
npm run build
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
npm run test:e2e

# 部署配置
Set-Location ..
powershell -File deploy/tests/config-check.ps1
docker compose --env-file deploy/.env.prod.example -f deploy/compose.prod.yml config --quiet
docker compose --env-file deploy/.env.staging.example -f deploy/compose.staging.yml config --quiet
```

## 4. 证据规则

- 功能任务至少保留单元/集成测试、构建结果和关键 E2E。
- 安全任务保留状态码、响应字段、数据库密文检查或自动化测试名称。
- 性能任务必须记录硬件、数据量、并发、持续时间、P95、错误率和首屏指标。
- 部署任务保留 Compose 校验、健康检查、备份 SHA256、恢复计数和回滚结果。
- 测试结论必须对应可重复命令；不得只记录“人工确认正常”。
- 生产域名、证书链、公网端口扫描和真实镜像回滚在目标云环境完成后补入本索引。

## 5. 已知非阻断项

| 项目 | 状态 | 说明 |
|---|---|---|
| Element Plus 全量引入 | 待优化 | 当前性能达标，但主包超过 500 kB 警告 |
| 部分资源图标 | 待优化 | 外部或旧相对地址可能失败，卡片使用首字母回退 |
| 云端 staging 实链路 | 待部署 | 需要真实域名、证书、镜像仓库和服务器访问权限 |
| 手机/微信登录、密码找回、举报和通知 | 不在范围 | 保留到后续版本 |

## 6. 验收结论

- P0 与 P1 核心功能链路已通过桌面和移动端 E2E。
- Phase 2 安全能力已通过自动化测试和本地运行时验证。
- 性能满足 AC-NFR-001、AC-NFR-002。
- 发布、备份、恢复、监控和回滚文档齐全，本地 Docker staging 已完成三容器启动验收。
- 真实云服务器上线前，仍需按第 4 节在目标环境完成域名、TLS、端口和镜像回滚复核。
