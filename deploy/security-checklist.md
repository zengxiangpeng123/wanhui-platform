# 万汇网 MVP 上线前安全检查表

| 项目 | 内容 |
|---|---|
| 文档编号 | SEC-CHECK-001 |
| 版本 | 1.1 |
| 更新日期 | 2026-11-27 |
| 适用范围 | MVP（Task 1–17） |
| 自动化证据 | `backend/src/test/java/com/wanhui/security/SecurityHardeningTest.java`、`InputSafetyTest.java` |

## 1. 认证与会话

| 检查项 | 状态 | 证据 |
|---|---|---|
| 密码使用 BCrypt 存储，绝不明文落库 | ✅ 已完成 | `SecurityHardeningTest.passwordIsStoredAsBcryptHashNotPlaintext` |
| 登录失败返回统一 401 且不泄露账号是否存在 | ✅ 已完成 | `AuthControllerTest`、`OpenApiContractTest` |
| Session 使用 HttpOnly Cookie（`WH_SESSION`） | ✅ 已完成 | `SecurityConfig.cookieSerializer` |
| 退出登录清理服务端 Session 与本地状态 | ✅ 已完成 | `AuthController.logout`、`stores/auth.ts` |
| 生产环境 `SESSION_COOKIE_SECURE=true` | ✅ 配置就绪 | staging/prod Compose 已固定为 `true`；真实域名 HTTPS 仍待云端复核 |

## 2. CSRF 与写操作

| 检查项 | 状态 | 证据 |
|---|---|---|
| 写操作缺少 CSRF Token 返回 403 | ✅ 已完成 | `SecurityHardeningTest.missingCsrfTokenIsRejected` |
| 前端从 `XSRF-TOKEN` Cookie 读取并写入 `X-XSRF-TOKEN` 头 | ✅ 已完成 | `frontend/src/api/http.ts` |
| 使用 `CsrfTokenRequestAttributeHandler` 保证 Cookie→Header 一致 | ✅ 已完成 | `SecurityConfig` |
| 登录/注册接口按设计豁免 CSRF | ✅ 已完成 | `SecurityConfig` ignoringRequestMatchers |

## 3. 权限与越权

| 检查项 | 状态 | 证据 |
|---|---|---|
| 未登录写请求返回 401 | ✅ 已完成 | `InputSafetyTest.unauthenticatedWriteIsRejected` |
| 普通用户访问管理员接口返回 403 | ✅ 已完成 | `SecurityHardeningTest.normalUserCannotAccessAdminApi` |
| 资源/帖子/网站操作按当前用户隔离 | ✅ 已完成 | `UserResourceControllerTest`、`InteractionControllerTest` |
| 管理员删帖写审计日志 | ✅ 已完成 | `AdminPostControllerTest` |

## 4. 输入与输出安全

| 检查项 | 状态 | 证据 |
|---|---|---|
| 标题 ≤100、正文 ≤20000、评论 ≤1000 字符校验 | ✅ 已完成 | `InputSafetyTest.overlongTitleIsRejected/overlongContentIsRejected/overlongCommentIsRejected` |
| 脚本输入按 JSON 字符串返回，前端 Vue 插值转义 | ✅ 已完成 | `SecurityHardeningTest.scriptInputIsReturnedAsJsonNotHtml` |
| 自定义网站仅允许 http/https，拒绝 `javascript:` 等协议 | ✅ 已完成 | `SecurityHardeningTest.illegalUrlSchemeIsRejected` |
| SQL 特殊字符安全存储，使用参数化查询 | ✅ 已完成 | `InputSafetyTest.sqlSpecialCharactersAreStoredSafely` |
| 重复注册/重复请求返回 409 或幂等结果 | ✅ 已完成 | `InputSafetyTest.duplicateRegistrationReturnsConflict`、收藏/点赞幂等测试 |

## 5. 数据与密钥

| 检查项 | 状态 | 证据 |
|---|---|---|
| 邮箱/手机号使用 AES-GCM 加密 + HMAC 索引 | ✅ 已完成 | `FieldEncryptionTest`、`EncryptedUserLookupTest` |
| 接口不返回完整邮箱/手机号/密码哈希 | ✅ 已完成 | DTO 仅返回 `emailMasked`/`phoneMasked` |
| 开发默认密钥不得用于生产 | ✅ 配置就绪 | Compose 使用必填变量守卫；生产值由 KMS/Secrets 注入 |
| 生产启用 KMS 与密钥轮换 | ✅ 已完成 | Task 22：KmsKeyProvider / KeyRotationService / FieldEncryptionMigrationService；证据 KeyRotationTest、FieldEncryptionMigrationTest |

## 6. 传输与部署

| 检查项 | 状态 | 证据 |
|---|---|---|
| 生产强制 HTTPS | ✅ 配置就绪 | Nginx 80 端口 301 到 HTTPS，TLS 1.2/1.3；真实域名待云端复核 |
| 安全响应头（HSTS、X-Content-Type-Options 等） | ✅ 配置就绪 | Nginx 配置 HSTS、nosniff、DENY、Referrer/Permissions Policy |
| 数据库不暴露公网端口 | ✅ 配置就绪 | staging/prod Compose 仅 Nginx 发布 80/443；需在云端再次扫描 |
| 登录/注册/评论限流返回 429 | ✅ 已完成 | Task 23：RateLimitTest（令牌桶 + HTTP 429 RATE_LIMITED） |
| 结构化 JSON 日志与 traceId | ✅ 已完成 | Task 24：ECS JSON + traceId；证据 HealthEndpointTest |
| 生产 Secrets 不入镜像 | ✅ 已完成 | Task 25：config-check.ps1 通过；.dockerignore 排除 .env* |
| Actuator liveness/readiness/Prometheus | ✅ 已完成 | Task 24：HealthEndpointTest 3/3 通过 |
| 日志不记录密码/Token/完整敏感字段 | ✅ 已完成 | `GlobalExceptionHandler` 仅记录 traceId 与异常 |

## 7. 结论

- **MVP 代码级安全测试与 Phase 2 安全验收全部通过（34 个安全用例）**。
- HTTPS、安全头、生产密钥和 KMS 配置已完成；真实域名、证书链、公网端口扫描仍需在云端 staging/生产复核。
