# 万汇网 staging / 生产运维手册

| 项目 | 内容 |
|---|---|
| 文档编号 | OPS-RUNBOOK-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-26 |
| 适用环境 | staging / 生产 |

## 1. 架构与端口

| 组件 | 容器名 | 端口 | 公网暴露 |
|---|---|---|---|
| Nginx（SPA + 反向代理） | wanhui-nginx-staging | 80 / 443 | 是 |
| Spring Boot 后端 | wanhui-app-staging | 8080 | 否（仅内部网络） |
| MySQL | wanhui-mysql-staging | 3306 | 否（仅内部网络） |

数据流：浏览器 -> Nginx(443) -> 静态资源或 /api -> app:8080 -> mysql:3306。

## 2. 首次部署

```bash
cd deploy
cp .env.staging.example .env.staging
# 编辑 .env.staging，填入强密码与 32 字节 base64 密钥
bash scripts/generate-staging-certs.sh
docker compose --env-file .env.staging -f compose.staging.yml up -d --build
```

验证：

```bash
docker compose --env-file .env.staging -f compose.staging.yml ps
curl -k -I https://127.0.0.1/
curl -k https://127.0.0.1/api/v1/resources
```

## 3. 日常发布

1. 在 CI 构建新镜像并打 tag（例如 APP_IMAGE_TAG=2026-11-26-1）。
2. 记录当前运行 tag，作为回滚点。
3. 执行 APP_IMAGE_TAG=<new> docker compose --env-file .env.staging -f compose.staging.yml up -d。
4. 按《发布检查表》验证；失败则执行第 6 节回滚。

## 4. 备份

```bash
cd deploy
export DB_CONTAINER=wanhui-mysql-staging DB_NAME=wanhui DB_USERNAME=wanhui_app DB_PASSWORD=<db_password>
bash scripts/backup.sh ./backups
sha256sum -c ./backups/<file>.sha256
```

建议：每日全量 + 保留 14 天 + 异地复制；密钥必须独立备份，丢失将导致邮箱/手机号无法解密。

## 5. 恢复

```bash
docker compose --env-file .env.staging -f compose.staging.yml stop app
DB_CONTAINER=wanhui-mysql-staging DB_NAME=wanhui DB_USERNAME=root DB_PASSWORD=<root_password> bash scripts/restore.sh ./backups/<file>.sql.gz
docker compose --env-file .env.staging -f compose.staging.yml start app
```

## 6. 回滚

```bash
APP_IMAGE_TAG=<previous_tag> docker compose --env-file .env.staging -f compose.staging.yml up -d
docker compose --env-file .env.staging -f compose.staging.yml ps
```

注意事项：

- Flyway 迁移只向前，禁止回滚时自动删表；若新版本含破坏性迁移，需先评估兼容性。
- 回滚后重新执行关键业务冒烟（登录、资源列表、发帖、评论）。

## 7. 故障处理

| 症状 | 排查 | 处置 |
|---|---|---|
| 502/504 | docker compose logs app | 检查 app 健康、DB 连接、内存 |
| app 反复重启 | docker inspect 退出码 | 查看启动日志；回滚到上一 tag |
| MySQL 不可用 | docker compose logs mysql、磁盘空间 | 恢复最近备份；扩容磁盘 |
| 证书过期 | openssl x509 -in certs/server.crt -noout -dates | 替换证书并 restart nginx |
| 磁盘满 | df -h、du -sh backups | 清理旧备份、扩容；检查日志轮转 |
| 登录异常 | 检查 SESSION_COOKIE_SECURE=true 与 HTTPS | 确认反向代理 X-Forwarded-Proto 正常 |

## 8. 日志与监控

- 容器日志：docker compose --env-file .env.staging -f compose.staging.yml logs -f <service>
- 请求追踪：错误响应携带 traceId，可在 app 日志中检索。
- 监控与告警（Task 24）：错误率、P95 延迟、容器健康、磁盘与备份结果。
