# 万汇网平台

万汇网是一个面向设计、AI、学术与媒体资源使用者的资源导航和交流社区。项目已完成 MVP 与 Phase 2 交付，包含资源浏览与收藏、论坛、个人中心、安全加固、可观测性、自动化测试和 Docker 部署能力。

## 当前状态

| 项目 | 状态 |
|---|---|
| 产品范围 | MVP + Phase 2 已实现 |
| 代码分支 | `main` |
| 后端 | Spring Boot 4.2.0、Java 21、MyBatis-Plus Boot 4 Starter |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia |
| 数据库 | MySQL 8.0（Docker）、Flyway V1-V7 |
| 自动化 | JUnit 5、Vitest、Playwright、k6 |
| 部署 | Docker Compose + Nginx + HTTPS |
| 更新日期 | 2026-11-27 |

## 架构概览

```text
Browser
   │ HTTPS
   ▼
Nginx :80/:443
   ├─ /          -> Vue 3 静态资源
   └─ /api/v1/*  -> Spring Boot :8080
                         ├─ MySQL 8.0 :3306（仅容器内网）
                         └─ Actuator 健康检查与指标
```

生产环境只发布 Nginx 的 80/443 端口，Spring Boot 和 MySQL 不直接暴露到公网。认证使用服务端 Session、`HttpOnly` Cookie 和 CSRF Token；邮箱/手机号使用 AES-GCM 与 HMAC 索引保护。

## 功能范围

- 资源目录：分类、搜索、详情、收藏和 202 条版本化资源数据。
- 论坛：发帖、帖子列表、详情、搜索、评论、帖子/评论点赞、帖子收藏、软删除。
- 用户中心：个人资料、修改密码、收藏、最近访问和自定义网站。
- 管理：管理员删帖和审计日志。
- 安全：会话认证、CSRF、权限隔离、输入校验、字段加密、KMS 兼容密钥轮换和限流。
- 运维：健康检查、Prometheus 指标、ECS JSON 日志、备份恢复、发布与回滚脚本。

不在当前范围：手机/微信登录、密码找回、举报审核、用户封禁、站内通知和资源管理后台。

## 目录结构

```text
wanhui-platform/
├─ backend/                 Spring Boot 后端与测试
├─ frontend/                Vue SPA、Vitest 与 Playwright E2E
├─ deploy/                  Docker Compose、Nginx、备份发布脚本
├─ docs/
│  ├─ api/                  API 使用说明
│  ├─ acceptance/           测试与验收记录
│  ├─ operations/           运维、发布和演练记录
│  └─ content/              站点文案
├─ .github/workflows/       CI
└─ README.md
```

## 前置条件

- JDK 21。项目使用 Java 21 编译，JDK 17 不能作为后端运行环境。
- Node.js 24 LTS 与 npm 11。
- Docker Desktop 或 Docker Engine，支持 `docker compose`。
- Git。
- 后端自带 Maven Wrapper，无需预装 Maven。

先确认版本：

```powershell
java -version
node --version
npm --version
docker version
docker compose version
```

### 配置 JDK 21

Windows PowerShell：

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

macOS/Linux：

```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

## 本地启动

以下命令从仓库根目录开始。

### 1. 启动 MySQL

```powershell
docker compose -f deploy/compose.dev.yml up -d --wait
docker compose -f deploy/compose.dev.yml ps
```

开发数据库连接信息：

| 项目 | 值 |
|---|---|
| 容器 | `wanhui-mysql-dev` |
| 地址 | `127.0.0.1:3308` |
| 数据库 | `wanhui_dev` |
| 开发用户 | `wanhui_app` |
| 开发用户密码 | `local_only_password` |
| root 密码 | `local_root_password` |

这些密码只允许用于本机开发，不得用于 staging 或生产环境。Flyway 会在后端启动时自动迁移数据库。

### 2. 启动后端

```powershell
Set-Location backend
.\mvnw.cmd -B -s ..\maven-settings.xml spring-boot:run
```

macOS/Linux：

```bash
cd backend
./mvnw -B -s ../maven-settings.xml spring-boot:run
```

验证：

- API：`http://127.0.0.1:8080/api/v1/resources`
- 健康检查：`http://127.0.0.1:8080/actuator/health`
- OpenAPI：`http://127.0.0.1:8080/v3/api-docs`
- Swagger UI：`http://127.0.0.1:8080/swagger-ui/index.html`

### 3. 启动前端

新开终端，从仓库根目录执行：

```powershell
Set-Location frontend
npm ci
npm run dev
```

浏览器访问 `http://127.0.0.1:5173`。Vite 会将 `/api` 代理到 `http://127.0.0.1:8080`。

## 验证与测试

### 后端

```powershell
Set-Location backend
.\mvnw.cmd -B -s ..\maven-settings.xml clean test
```

### 前端单元测试与生产构建

```powershell
Set-Location frontend
npm ci
npm run test:unit:run
npm run build
```

构建会报告 Element Plus 全量引入导致主包超过 500 kB 的警告。这是当前已知的性能优化项，不阻断功能发布。

### 端到端测试

确保 Docker MySQL 已启动。Playwright 会启动或复用前后端服务器，并在本地开发数据库中创建带随机后缀的测试数据：

```powershell
Set-Location frontend
npx playwright install chromium
npm run test:e2e
```

Playwright 自行启动后端时会为测试进程设置 `WANHUI_SECURITY_RATE_LIMIT_ENABLED=false`，避免批量注册触发限流。若复用已经启动的后端，则需要先用同一变量启动它：

```powershell
$env:WANHUI_SECURITY_RATE_LIMIT_ENABLED = 'false'
```

限流行为必须继续由后端 `RateLimitTest` 覆盖，不能通过关闭限流替代安全测试。

### 安全与部署配置检查

```powershell
Set-Location backend
.\mvnw.cmd -B -s ..\maven-settings.xml test '-Dtest=SecurityHardeningTest,InputSafetyTest,RateLimitTest,KeyRotationTest,FieldEncryptionMigrationTest'

Set-Location ..
powershell -File deploy/tests/config-check.ps1
docker compose --env-file deploy/.env.prod.example -f deploy/compose.prod.yml config --quiet`ndocker compose --env-file deploy/.env.staging.example -f deploy/compose.staging.yml config --quiet
```

三个命令必须全部以退出码 0 结束。

## API 使用

所有业务 API 位于 `/api/v1`，统一返回：

```json
{"success":true,"data":{}}
```

错误响应包含 `error.code`、`error.message`、可选的 `error.fieldErrors` 和 `traceId`。写操作必须先获取 CSRF Token，并在请求头发送 `X-XSRF-TOKEN`；登录态通过 `WH_SESSION` Cookie 维持。

完整端点、认证示例和错误码见 [API 使用说明](docs/api/README.md)。

## Staging 部署

1. 在 `deploy/` 下复制环境模板并填写强密码和 32 字节 Base64 密钥：

```bash
cd deploy
cp .env.staging.example .env.staging
bash scripts/generate-staging-certs.sh
```

2. Docker 构建默认使用国内依赖镜像，也可通过参数覆盖：

```bash
cd deploy
docker compose --env-file .env.staging -f compose.staging.yml build \
  --build-arg MAVEN_MIRROR_URL=https://repo.maven.apache.org/maven2 \
  --build-arg NPM_REGISTRY_URL=https://registry.npmjs.org
```

默认值：Maven `https://maven.aliyun.com/repository/public`，npm `https://registry.npmmirror.com`。

3. 校验生产配置约束：
```powershell
powershell -File deploy/tests/config-check.ps1
```

4. 构建并启动：

```bash
cd deploy
docker compose --env-file .env.staging -f compose.staging.yml up -d --build
docker compose --env-file .env.staging -f compose.staging.yml ps
```

5. 验证 HTTPS 与 API：

```bash
curl -kI https://127.0.0.1/
curl -k https://127.0.0.1/api/v1/resources
curl -k https://127.0.0.1/api/v1/auth/csrf
```

生产部署、备份、恢复、发布、回滚、故障处理和密钥轮换见 [运维手册](docs/operations/runbook.md)。

## 环境变量

生产环境至少需要以下变量，真实值不得提交到 Git：

| 变量 | 用途 |
|---|---|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `DB_NAME` | 数据库名 |
| `DB_USERNAME` / `DB_PASSWORD` | 应用数据库账号 |
| `FIELD_KEY_VERSION` | 当前字段密钥版本 |
| `FIELD_ENCRYPTION_KEY` | 32 字节 Base64 AES-GCM 密钥 |
| `FIELD_HMAC_KEY` | 32 字节 Base64 HMAC 密钥 |
| `SESSION_COOKIE_SECURE` | 生产必须为 `true` |
| `APP_IMAGE_TAG` / `FRONTEND_IMAGE_TAG` | 可回滚镜像标签 |
| `LOG_LEVEL` | 日志级别 |

生产密钥应由云 KMS 或 Secrets Manager 注入。密钥丢失会导致已加密字段无法恢复，必须独立备份并制定轮换计划。

## 性能基线

2026-11-27 在 8 核 16 线程、27.8 GiB 内存的 Windows 11 开发机上完成：

- API：20 VU 持续 5 分钟，P95 小于 2 秒，错误率小于 1%。
- 首页：生产构建、20 Mbps 下行、5 Mbps 上行、50 ms 延迟、禁用缓存，首屏 LCP 小于 3 秒。

原始结果与复现命令见 [性能验收记录](docs/acceptance/performance.md)。

## 项目文档

- [API 使用说明](docs/api/README.md)
- [验收证据索引](docs/acceptance/README.md)
- [MVP E2E 记录](docs/acceptance/2026-11-24-e2e-record.md)
- [Phase 2 安全验收](docs/acceptance/phase2-security.md)
- [性能验收](docs/acceptance/performance.md)
- [运维手册](docs/operations/runbook.md)
- [生产检查表](docs/operations/production-checklist.md)
- [发布检查表](docs/operations/release-checklist.md)
- [备份恢复演练记录](docs/operations/drill-record-2026-11-26.md)

## 安全约束

- 不在仓库、镜像或日志中写入密码、Session、Token、完整邮箱/手机号和密钥。
- 生产环境强制 HTTPS、Secure/HttpOnly/SameSite Cookie、HSTS 和安全响应头。
- 所有写请求必须经过 CSRF、服务端鉴权、输入校验和用户数据隔离。
- 数据库端口只存在于 Docker 私有网络。
- 发布前必须完成备份、健康检查、关键业务冒烟和回滚点记录。
