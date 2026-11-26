# 万汇网生产发布检查表

| 项目 | 内容 |
|---|---|
| 文档编号 | OPS-PROD-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-26 |
| 对应任务 | Task 25 生产部署、Secrets/KMS 与发布检查 |

## 1. 发布前

- [ ] 目标提交 CI 全绿（后端测试、前端单测、E2E、安全测试、健康检查）。
- [ ] 后端与前端镜像已构建并推送到镜像仓库，tag 固定为 `YYYY-MM-DD-N`。
- [ ] `deploy/.env.prod` 已生成（权限 600），且未被提交到 Git。
- [ ] `MYSQL_ROOT_PASSWORD`、`DB_PASSWORD`、`FIELD_ENCRYPTION_KEY`、`FIELD_HMAC_KEY` 均由 KMS/Secrets 注入。
- [ ] TLS 证书有效且剩余天数 > 14 天。
- [ ] 已执行 `deploy/tests/config-check.ps1` 并通过。
- [ ] 已执行一次数据库备份并校验 SHA-256。
- [ ] 记录当前 `APP_IMAGE_TAG` / `FRONTEND_IMAGE_TAG` 作为回滚点。

## 2. 部署

```bash
cd deploy
APP_IMAGE_TAG=<new> FRONTEND_IMAGE_TAG=<new> bash scripts/release.sh
```

- [ ] `docker compose -f compose.prod.yml ps` 中 mysql/app/nginx 均为 healthy。
- [ ] 仅 Nginx 暴露 80/443；MySQL 3306 与 app 8080 不可从公网访问。
- [ ] `docker compose logs app` 无异常堆栈与敏感字段。
- [ ] `/actuator/health/liveness`、`/actuator/health/readiness` 返回 UP（内网访问）。

## 3. 发布后验证

- [ ] `https://<域名>/` 返回 200 且 SPA 正常加载。
- [ ] `https://<域名>/api/v1/resources` 返回 `success:true`。
- [ ] 注册 → 登录 → 刷新 → 退出 正常。
- [ ] 资源收藏、论坛发帖/评论/点赞/搜索 正常。
- [ ] 个人资料保存与密码修改 正常。
- [ ] 移动端 375px 无横向滚动。
- [ ] 响应头包含 HSTS、X-Content-Type-Options、X-Frame-Options。
- [ ] 错误响应包含 `traceId`，结构化日志可按 traceId 检索。

## 4. 回滚

触发条件（任一）：

- 核心流程不可用超过 5 分钟。
- 错误率持续 > 5%。
- 数据库迁移导致旧数据不可读。

步骤：

- [ ] `bash scripts/rollback.sh`
- [ ] 确认 healthcheck 通过。
- [ ] 重跑第 3 节关键验证。
- [ ] 记录故障时间线、影响范围与根因到 `.release-state` 或运维记录。

## 5. 安全与合规

- [ ] Compose 中无明文密码/密钥（`config-check.ps1` 已校验）。
- [ ] `.env.prod`、`.release-state`、证书私钥均被 `.gitignore` 忽略。
- [ ] 生产镜像以非 root 用户运行。
- [ ] 数据库端口未映射到宿主机。
- [ ] 审计日志保留策略与备份保留策略已确认。

## 6. 记录

| 字段 | 内容 |
|---|---|
| 发布版本 | |
| 发布时间 | |
| 发布人 | |
| 回滚点 | |
| 验证结果 | |
| 是否回滚 | |
