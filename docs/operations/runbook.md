# 万汇网 staging / 生产运维手册

| 项目 | 内容 |
|---|---|
| 文档编号 | OPS-RUNBOOK-001 |
| 版本 | 1.1 |
| 更新日期 | 2026-11-27 |
| 适用环境 | staging / 生产 |
| 代码基线 | 万汇网 MVP + Phase 2 |

## 1. 架构、端口与职责

| 组件 | 容器名示例 | 内部端口 | 公网暴露 |
|---|---|---|---|
| Nginx（SPA + 反向代理） | `wanhui-nginx-staging` / `wanhui-nginx-prod` | 80 / 443 | 是 |
| Spring Boot 后端 | `wanhui-app-staging` / `wanhui-app-prod` | 8080 | 否 |
| MySQL 8.0 | `wanhui-mysql-staging` / `wanhui-mysql-prod` | 3306 | 否 |

数据流：

```text
Browser -> Nginx :443 -> 静态 SPA
                     -> /api/* -> app:8080 -> mysql:3306
```

生产只允许发布 Nginx 的 80/443。数据库不得映射到宿主机公网地址。

## 2. 上线前置条件

- Linux 服务器建议 2 vCPU / 4 GiB 内存起步；磁盘容量需覆盖镜像、日志与至少 14 天备份。
- Docker Engine 24+ 与 Docker Compose v2。
- 已解析域名和有效 TLS 证书；staging 可使用自签名证书。
- 仓库版本已通过 CI，发布产物 tag 已不可变。
- 已准备 32 字节 Base64 AES-GCM 密钥和 HMAC 密钥；生产环境优先由 KMS/Secrets Manager 注入。
- 已确认最近一次数据库备份可校验、可恢复。

## 3. 首次部署

### 3.1 配置环境

```bash
cd deploy
cp .env.staging.example .env.staging
# 编辑 .env.staging：强密码、域名、字段密钥、镜像 tag
bash scripts/generate-staging-certs.sh
```

生产环境复制 `.env.prod.example` 为 `.env.prod`。`.env*` 已被 Git 忽略，不得提交、复制到镜像或写入工单。

必须替换的值：

```text
MYSQL_ROOT_PASSWORD
DB_PASSWORD
FIELD_KEY_VERSION
FIELD_ENCRYPTION_KEY
FIELD_HMAC_KEY
APP_IMAGE_TAG
FRONTEND_IMAGE_TAG（生产）
```

`FIELD_ENCRYPTION_KEY` 与 `FIELD_HMAC_KEY` 必须各为 32 字节 Base64。丢失加密密钥会导致历史邮箱/手机号无法解密。

### 3.2 预检

```powershell
# Windows 开发机
powershell -File deploy/tests/config-check.ps1
docker compose -f deploy/compose.prod.yml config --quiet
```

```bash
# Linux 服务器
cd deploy
docker compose --env-file .env.staging -f compose.staging.yml config --quiet
```

预期输出：

```text
CONFIG CHECK PASSED: no plaintext secrets, only 80/443 exposed, required guards present.
```

任一命令失败时禁止继续发布。

### 3.3 构建镜像

Docker 构建默认使用国内依赖镜像：

- Maven：`https://maven.aliyun.com/repository/public`
- npm：`https://registry.npmmirror.com`

服务器网络区域不同或需要使用企业私服时，用构建参数覆盖：

```bash
cd deploy
docker compose --env-file .env.staging -f compose.staging.yml build \
  --build-arg MAVEN_MIRROR_URL=https://repo.maven.apache.org/maven2 \
  --build-arg NPM_REGISTRY_URL=https://registry.npmjs.org
```

前端镜像使用 `npm ci`，`package-lock.json` 已兼容 Node 22 / npm 10 与 Node 24 / npm 11。锁文件不一致时构建必须失败并修复，不得在镜像中改用 `npm install` 绕过。

### 3.4 启动 staging
```bash
cd deploy
docker compose --env-file .env.staging -f compose.staging.yml up -d --build
docker compose --env-file .env.staging -f compose.staging.yml ps
```

验证：

```bash
curl -kI https://127.0.0.1/
curl -kfsS https://127.0.0.1/api/v1/resources >/dev/null
curl -kfsS https://127.0.0.1/api/v1/auth/csrf >/dev/null
docker compose --env-file .env.staging -f compose.staging.yml logs --tail=100 app nginx mysql
```

预期：首页 200，API 200，所有容器处于 `healthy` 或 `running`。通过域名访问时确认 HTTP 301 到 HTTPS，响应含 HSTS。

### 3.5 首次生产部署

生产镜像应由 CI 构建并推送到镜像仓库，不在生产服务器使用 `build`：

```bash
cd deploy
APP_IMAGE_TAG=2026-11-27-1 \
FRONTEND_IMAGE_TAG=2026-11-27-1 \
./scripts/release.sh
```

脚本会保存 `.release-state`，切换 `.env.prod` 中的镜像 tag，启动 Compose 并轮询 HTTPS API。脚本失败时必须执行第 7 节回滚。

## 4. 日常发布

1. 确认 CI 后端、前端、E2E 和安全检查通过。
2. 记录当前运行 tag；发布脚本会自动写入 `.release-state`。
3. 发布前执行一次数据库备份并校验 SHA256。
4. 使用新 tag 执行 `release.sh`。
5. 完成发布后冒烟：
   - 打开首页，确认资源列表加载。
   - 登录、发帖、评论、点赞、收藏各执行一次。
   - 检查 `/api/v1/resources`、健康指标和错误日志。
   - 检查无 5xx、无高频 429、无数据库连接池耗尽。
6. 更新发布记录和变更单。

```bash
cd deploy
APP_IMAGE_TAG=<new-app-tag> \
FRONTEND_IMAGE_TAG=<new-frontend-tag> \
./scripts/release.sh
```

禁止直接修改已发布 tag 指向的镜像。需要修复时发布新 tag。

## 5. 备份

```bash
cd deploy
export DB_CONTAINER=wanhui-mysql-prod \
       DB_NAME=wanhui \
       DB_USERNAME=wanhui_app \
       DB_PASSWORD=<db_password>
bash scripts/backup.sh ./backups
sha256sum -c ./backups/<file>.sha256
```

备份要求：

- 每日全量备份，至少保留 14 天。
- 备份文件异地复制，并限制读取权限。
- 密钥独立备份；数据库备份和密钥必须分别受控。
- 每月至少抽检一次恢复，不能只验证备份文件存在。

## 6. 恢复

恢复会覆盖目标数据库，属于高风险操作。执行前必须确认目标环境、备份文件和停机窗口。

```bash
cd deploy
docker compose --env-file .env.prod -f compose.prod.yml stop app
DB_CONTAINER=wanhui-mysql-prod \
DB_NAME=wanhui \
DB_USERNAME=root \
DB_PASSWORD=<root_password> \
bash scripts/restore.sh ./backups/<file>.sql.gz
docker compose --env-file .env.prod -f compose.prod.yml start app
curl -kfsS https://127.0.0.1/api/v1/resources >/dev/null
```

恢复后检查：

- Flyway schema history 版本符合预期。
- 用户、帖子、评论和审计记录数量与恢复报告一致。
- 登录、资源列表、发帖、评论和收藏冒烟通过。
- 应用日志无字段解密异常；如异常，检查密钥版本是否与备份匹配。

## 7. 回滚

```bash
cd deploy
./scripts/rollback.sh
docker compose --env-file .env.prod -f compose.prod.yml ps
```

回滚规则：

- Flyway 迁移只向前，禁止自动删表或回退 schema。
- 发布前必须判断新迁移是否兼容旧应用版本。
- 若回滚后仍不健康，立即停止写流量，保留日志和容器状态，并按最近备份恢复。
- 回滚后重新执行登录、资源列表、发帖、评论和收藏冒烟。

## 8. 故障处理

| 症状 | 排查 | 处置 |
|---|---|---|
| 502/504 | `docker compose logs app nginx` | 检查 app 健康、DB 连接、内存和超时 |
| app 反复重启 | `docker inspect <container>` 查看退出码 | 查看启动日志；修复配置或回滚 |
| MySQL 不可用 | `docker compose logs mysql`、磁盘、连接数 | 恢复最近备份、扩容或重启 |
| 证书过期 | `openssl x509 -in certs/server.crt -noout -dates` | 替换证书并重启 Nginx |
| 磁盘满 | `df -h`、`du -sh backups` | 清理过期备份、日志轮转、扩容 |
| 登录异常 | 检查 HTTPS、`SESSION_COOKIE_SECURE=true`、`X-Forwarded-Proto` | 修正代理头或 Cookie 配置 |
| 写请求 403 | 检查 CSRF Cookie 与 `X-XSRF-TOKEN` 请求头 | 刷新 CSRF 后重试；检查同源策略 |
| 高频 429 | 查看登录/注册/评论限流配置 | 确认不是攻击或异常重试；按安全流程处置 |
| 字段解密失败 | 检查密钥版本与应用日志 | 恢复正确密钥，不得输出明文排查 |

## 9. 日志与监控

```bash
docker compose --env-file .env.prod -f compose.prod.yml logs -f app
docker compose --env-file .env.prod -f compose.prod.yml logs -f nginx
```

- 应用日志为 ECS JSON，包含 `traceId`、HTTP 方法、路径、状态码和耗时。
- 日志不记录密码、Token、Session、完整邮箱/手机号或密钥。
- Actuator 指标只在容器内网开放；Prometheus 通过内网抓取。
- 告警至少覆盖：5xx 错误率、P95 延迟、容器健康、CPU/内存、磁盘、数据库连接池、备份失败和证书到期。
- 日志保留与云平台合规要求一致，避免无限占用磁盘。

## 10. 密钥轮换

1. 在 KMS/Secrets 中创建新版本密钥（版本号在当前版本上加 1），保留旧版本只读权限。
2. 更新应用环境变量 `FIELD_KEY_VERSION` 并重启，使新写入使用新版本。
3. 触发 `FieldEncryptionMigrationService.migrate(batchSize)` 分批迁移历史密文。
4. 核对 `users.email_key_version`、`users.phone_key_version` 是否全部为新版本。
5. 完成业务冒烟和数据抽样后，再计划禁用/删除旧版本密钥。

约束：

- 迁移按 id 游标分批，可重复执行和续跑。
- 单行失败只记录用户 ID 和异常类型，不记录明文、密文样本或密钥。
- 回滚应用镜像时保留兼容旧密钥版本；不得提前删除旧密钥。

## 11. 发布后检查与记录

- [ ] 所有容器健康，80/443 可达，3306/8080 未公网暴露。
- [ ] HTTPS、HSTS、TLS 1.2/1.3 和安全响应头正常。
- [ ] `WH_SESSION` 为 Secure + HttpOnly + SameSite=Lax。
- [ ] 首页、资源列表、登录、发帖、评论、点赞、收藏冒烟通过。
- [ ] 5xx、P95、限流、数据库连接池和磁盘无异常。
- [ ] 最新备份可校验，回滚点和镜像 tag 已记录。
- [ ] 发布结果、验证人、时间和异常处理已写入变更记录。

关联文档：

- [生产检查表](./production-checklist.md)
- [发布检查表](./release-checklist.md)
- [备份恢复演练记录](./drill-record-2026-11-26.md)
- [Phase 2 安全验收](../acceptance/phase2-security.md)
