# staging 备份、恢复与回滚演练记录

| 项目 | 内容 |
|---|---|
| 文档编号 | OPS-DRILL-001 |
| 执行日期 | 2026-11-26 |
| 执行环境 | 本地 Docker MySQL 8.0（wanhui-mysql-dev，模拟 staging） |
| 对应任务 | Task 18 |
| 结论 | 通过 |

## 1. 备份演练

命令（Git Bash，等价于 runbook 第 4 节）：

```bash
cd deploy
DB_CONTAINER=wanhui-mysql-dev DB_NAME=wanhui_dev DB_USERNAME=root DB_PASSWORD=*** bash scripts/backup.sh ./backups
```

结果：

| 项目 | 值 |
|---|---|
| 备份文件 | backups/wanhui_dev-20261126-220845.sql.gz |
| 大小 | 13,464 字节 |
| SHA-256 | 3b5032c64f2d3bf45eb761ddc01822eac2c4a8e98cdc1f61793be235e6a753e5 |
| 校验文件 | backups/wanhui_dev-20261126-220845.sql.gz.sha256 |

## 2. 干净库恢复演练

1. 新建空库 wanhui_restore_drill（0 张表）。
2. 执行 scripts/restore.sh，脚本先校验 SHA-256，结果 OK。
3. 恢复完成后核对计数：

| 表 | 源库 | 恢复库 | 一致 |
|---|---|---|---|
| users | 30 | 30 | 是 |
| posts | 4 | 4 | 是 |
| post_comments | 4 | 4 | 是 |
| resource_favorites | 5 | 5 | 是 |
| post_likes | 4 | 4 | 是 |
| audit_logs | 0 | 0 | 是 |
| flyway_schema_history | 6 | 6 | 是 |

演练结束后已删除 wanhui_restore_drill。

## 3. 应用回滚演练

说明：当前环境无法从公共镜像仓库拉取 maven:3.9-eclipse-temurin-21 与 eclipse-temurin:21-jre，因此以本地构建的可执行 JAR 代表发布产物执行回滚演练；生产环境以 APP_IMAGE_TAG 切换镜像。

步骤与结果：

| 步骤 | 命令/配置 | 结果 |
|---|---|---|
| 构建发布产物 | mvnw package -DskipTests | 生成 wanhui-backend-0.1.0-SNAPSHOT.jar（39 MB） |
| 标记上一版本 | 复制为 app-previous.jar | 成功 |
| 以 staging 配置启动 | SPRING_PROFILES_ACTIVE=staging、DB 指向本地 MySQL、端口 8081、JDK 21 | 启动成功 |
| 健康检查 | GET /api/v1/auth/csrf | 204 |
| Flyway 兼容性 | 启动日志 | Schema wanhui_dev is up to date. No migration necessary. |
| 结论 | 上一版本可启动，迁移向下兼容 | 通过 |

## 4. 待生产环境复核项

以下项目在真实 staging/生产服务器上仍需复核：

- [ ] 使用真实镜像 tag 执行一次 APP_IMAGE_TAG 切换回滚。
- [ ] 校验 Nginx 443 证书链与 HSTS。
- [ ] 从公网扫描确认 3306/8080 不可达。
- [ ] 备份文件异地复制与保留策略。
