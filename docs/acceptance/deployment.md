# 万汇网 Docker staging 部署验收

| 项目 | 内容 |
|---|---|
| 文档编号 | ACC-DEPLOY-001 |
| 版本 | 1.0 |
| 执行日期 | 2026-11-27（北京时间） |
| 对应任务 | Task 25、Task 29 |
| 环境 | 本机 Docker Desktop，模拟 staging |
| 结论 | 镜像构建、HTTPS、健康检查、Flyway 与安全 Cookie 均通过；真实云域名待部署复核 |

## 1. 验收范围

- 从仓库源码构建前端和后端镜像。
- 使用 staging Compose 启动 Nginx、Spring Boot 和 MySQL。
- 验证 Nginx 80 端口跳转 HTTPS、443 首页/API 可访问。
- 验证安全响应头、Secure Session Cookie、Flyway V7。
- 验证 MySQL 3306 与后端 8080 未映射到宿主机。

## 2. 构建过程中发现并修复的问题

| 问题 | 影响 | 修复 |
|---|---|---|
| npm 11 生成的锁文件缺少 npm 10 需要的可选依赖条目 | Node 22 镜像内 `npm ci` 失败 | 使用 Node 22/npm 10 重新生成 `package-lock.json`，保留锁文件严格安装 |
| Docker 网络访问 Maven Central 返回 403 | 后端镜像无法下载依赖 | 增加可配置 `MAVEN_MIRROR_URL`，默认使用阿里云公共仓库，CI 可覆盖为官方 Central |
| Docker Hub/镜像层下载缓慢 | 构建时间不可控 | 使用 BuildKit 缓存挂载；镜像构建放入 CI 门禁 |
| 最终镜像为安装 `curl` 执行 `apt-get`，受 Ubuntu 镜像网络波动影响 | 后端镜像构建不稳定 | 移除运行镜像中的 curl 安装，健康检查改为容器内 TCP 端口探针 |

## 3. 执行命令

```powershell
cd D:\dev\codex\me\wanhuiwang\wanhui-platform
docker compose --env-file deploy/.env.staging -f deploy/compose.staging.yml build

docker compose -p wanhui-staging-verify `
  --env-file ..\Codex工作区\2026-11-26_002_性能基线\staging-verify.env `
  -f deploy/compose.staging.yml up -d
```

说明：演练环境使用临时生成的有效 Base64 密钥和密码文件，文件位于本机 Codex 工作区，不进入仓库。真实部署必须使用 KMS/Secrets 注入。

## 4. 实测结果

| 检查项 | 结果 |
|---|---|
| 前端镜像 | `wanhui-frontend:staging` 构建成功 |
| 后端镜像 | `wanhui-backend:staging` 构建成功 |
| MySQL | `wanhui-mysql-staging` healthy |
| Spring Boot | `wanhui-app-staging` healthy |
| Nginx | `wanhui-nginx-staging` healthy |
| HTTPS 首页 | HTTP 200 |
| HTTPS API | `GET /api/v1/resources` HTTP 200 |
| HTTP 跳转 | HTTP 301 -> `https://127.0.0.1/` |
| HSTS | `max-age=31536000; includeSubDomains` |
| 安全响应头 | nosniff、DENY、Referrer-Policy、Permissions-Policy 均存在 |
| Flyway | 最新版本 V7，success=1 |
| Session Cookie | `WH_SESSION` 带 `Secure; HttpOnly; SameSite=Lax` |
| CSRF Cookie | `XSRF-TOKEN` 带 `Secure` |
| 公网端口 | 仅 Nginx 80/443 映射；MySQL 3306 和 app 8080 仅容器内网 |

## 5. 冒烟流程

1. 通过 HTTPS 注册新用户，返回 201。
2. 使用 `account` + `password` 登录，返回 200。
3. 响应设置 Secure Session 与 CSRF Cookie。
4. 资源 API 返回 200。
5. HTTP 入口返回 301 并跳转 HTTPS。

## 6. 待云端复核

- [ ] 使用真实域名和受信任证书替换自签名证书。
- [ ] 从公网扫描确认 80/443 以外端口不可达。
- [ ] 使用镜像仓库中的不可变 tag 执行正式 `release.sh`。
- [ ] 在目标云环境执行一次备份、恢复和真实镜像 tag 回滚。
- [ ] 接入云 KMS/Secrets Manager 并验证密钥轮换。
- [ ] 配置 Prometheus、日志采集、磁盘和证书到期告警。

## 7. 结论

本地 staging 模拟已从源码完成镜像构建和三容器启动，HTTPS、健康检查、Flyway 和安全 Cookie 均通过。该结果证明交付文档中的构建与部署路径可执行；真实云服务器上线仍需完成第 6 节复核项。
