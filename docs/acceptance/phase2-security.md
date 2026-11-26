# 万汇网 Phase 2 安全与合规验收记录

| 项目 | 内容 |
|---|---|
| 文档编号 | ACC-SEC-P2-001 |
| 版本 | 1.0 |
| 执行日期 | 2026-11-26 |
| 代码基线 | main 分支（Task 25 完成） |
| 运行环境 | JDK 21.0.13、Spring Boot 4.2.0、MySQL 8.0（Docker，127.0.0.1:3308） |
| 覆盖验收项 | AC-SEC-001、AC-SEC-004、AC-SEC-005、AC-SEC-008 |
| 结论 | 本地可验证项全部通过；HTTPS/TLS 实链路待 staging 复核 |

## 1. AC-SEC-001：HTTPS、TLS、Cookie 与 HSTS

### 1.1 静态配置验证

| 检查项 | 证据 |
|---|---|
| TLS 1.2 / 1.3 | deploy/nginx/nginx.conf：ssl_protocols TLSv1.2 TLSv1.3; |
| HSTS | add_header Strict-Transport-Security max-age=31536000; includeSubDomains |
| X-Content-Type-Options | add_header X-Content-Type-Options nosniff |
| X-Frame-Options | add_header X-Frame-Options DENY |
| HTTP 到 HTTPS | Nginx 80 端口 return 301 https://$host$request_uri |
| 会话 Cookie HttpOnly | SecurityConfig：serializer.setUseHttpOnlyCookie(true) |
| 会话 Cookie SameSite | serializer.setSameSite(Lax) |
| 生产 Cookie Secure | setUseSecureCookie(secure)，生产 SESSION_COOKIE_SECURE=true |

### 1.2 运行时 Cookie 验证

实测登录响应头（本地 HTTP，Secure 按配置为 false）：

```text
XSRF-TOKEN=<uuid>; Path=/
WH_SESSION=<redacted>; Path=/; HttpOnly; SameSite=Lax
```

生产环境设置 SESSION_COOKIE_SECURE=true 后，WH_SESSION 将带 Secure。

### 1.3 待 staging 复核

- [ ] curl -I https://<域名>/ 返回 200，响应头含 HSTS。
- [ ] openssl s_client -connect <域名>:443 -tls1_2 与 -tls1_3 均握手成功。
- [ ] 浏览器确认会话 Cookie 带 Secure; HttpOnly; SameSite=Lax。
- [ ] HTTP 访问 301 跳转 HTTPS。

## 2. AC-SEC-005：字段密文、IV、HMAC、密钥版本与脱敏

### 2.1 数据库实测

注册用户 p2sec1795707619949 后查询：

```sql
SELECT HEX(email_ciphertext), LENGTH(email_iv), email_key_version,
       HEX(email_hmac), email_hmac_version, email_masked
FROM users WHERE username_normalized = 'p2sec1795707619949';
```

结果：

| 字段 | 值 |
|---|---|
| email_ciphertext | 二进制密文（非明文邮箱） |
| email_iv 长度 | 12 字节 |
| email_key_version | 1 |
| email_hmac | 32 字节 HMAC-SHA256 |
| email_hmac_version | 1 |
| email_masked | p2s***@example.com |

### 2.2 接口脱敏

GET /api/v1/users/me/profile 返回：

```json
{"username":"p2sec...","emailMasked":"p2s***@example.com","phoneMasked":null}
```

响应中不含完整邮箱、手机号、密码哈希或密钥。

### 2.3 自动化测试证据

| 测试 | 结果 |
|---|---|
| FieldEncryptionTest | 4/4 通过（加解密、IV、密钥版本） |
| EncryptedUserLookupTest | 1/1 通过（HMAC 索引查找） |
| FieldEncryptionMigrationTest | 3/3 通过（重加密、幂等、失败隔离） |
| KeyRotationTest | 4/4 通过（多版本解密与轮换） |
| UserProfileControllerTest | 6/6 通过（只读掩码字段） |

## 3. AC-SEC-004：生产 Secrets 不在镜像与仓库中

| 检查项 | 证据 |
|---|---|
| Compose 无明文密钥 | deploy/tests/config-check.ps1 输出 CONFIG CHECK PASSED |
| 必填变量守卫 | MYSQL_ROOT_PASSWORD、DB_PASSWORD、FIELD_ENCRYPTION_KEY 等均使用 :? 守卫 |
| .env.prod 不入库 | .gitignore 忽略 deploy/.env.prod |
| .env* 不入镜像 | backend/.dockerignore、frontend/.dockerignore 均含 .env、.env.* |
| 证书私钥不入库 | .gitignore 忽略 deploy/nginx/certs/*.key |
| 回滚状态不入库 | .gitignore 忽略 deploy/.release-state |

## 4. AC-SEC-008：限流

### 4.1 实测（默认登录 10 次/分钟）

连续 11 次错误登录，状态码序列：

```text
401,401,401,401,401,401,401,401,401,401,429
```

第 11 次响应体：

```json
{"success":false,"error":{"code":"RATE_LIMITED","message":"请求过于频繁，请稍后再试"},
 "traceId":"7b43d0450d8944dbbcad3f82ff5f4729"}
```

### 4.2 自动化测试证据

| 测试 | 结果 |
|---|---|
| RateLimitTest（单元：令牌桶容量/恢复） | 通过 |
| RateLimitTest（集成：HTTP 429 + 统一错误体） | 通过 |

## 5. 日志脱敏

| 检查项 | 证据 |
|---|---|
| 请求日志字段 | http_request method={} path={} status={} durationMs={} |
| 不记录 query/请求体 | TraceIdFilter 仅使用 getRequestURI() |
| traceId 贯穿 | ECS JSON 日志字段 traceId（MDC） |
| 异常日志 | GlobalExceptionHandler 仅记录 traceId 与异常 |

## 6. 版本与命令记录

```text
Java:          OpenJDK 21.0.13.1 LTS (Microsoft)
Spring Boot:   4.2.0
MySQL:         8.0 (Docker, wanhui-mysql-dev)
Flyway:        V1 到 V7
```

关键命令：

```powershell
.\mvnw.cmd -B test
.\mvnw.cmd -B test '-Dtest=SecurityHardeningTest,InputSafetyTest,RateLimitTest,KeyRotationTest,FieldEncryptionMigrationTest'
powershell -File deploy/tests/config-check.ps1
docker compose -f deploy/compose.prod.yml config --quiet
```

## 6.1 本次执行汇总

本次共运行 9 个安全相关测试类、**34 个测试用例，全部通过**（0 失败 0 错误）。

## 7. 结论

- AC-SEC-001：代码与 Nginx 配置满足要求；运行时 Cookie 已实测 HttpOnly/SameSite；HTTPS/TLS/HSTS 实链路留待 staging 复核。
- AC-SEC-004：Compose 无明文密钥，Secrets 通过环境变量/KMS 注入，镜像构建排除 .env*。
- AC-SEC-005：字段密文、12 字节 IV、32 字节 HMAC、密钥版本与脱敏响应均已实测并通过。
- AC-SEC-008：限流按预期返回 429 RATE_LIMITED，窗口恢复由可控时钟测试覆盖。

