# 万汇网 MVP 上线前安全检查表

| 项目 | 内容 |
|---|---|
| 文档编号 | SEC-CHECK-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-26 |
| 适用范围 | MVP（Task 1–17） |
| 自动化证据 | `backend/src/test/java/com/wanhui/security/SecurityHardeningTest.java`、`InputSafetyTest.java` |

## 1. 认证与会话

| 检查项 | 状态 | 证据 |
|---|---|---|
| 密码使用 BCrypt 存储，绝不明文落库 | ✅ 已完成 | `SecurityHardeningTest.passwordIsStoredAsBcryptHashNotPlaintext` |
| 登录失败返回统一 401 且不泄露账号是否存在 | ✅ 已完成 | `AuthControllerTest`、`OpenApiContractTest` |
| Session 使用 HttpOnly Cookie（`WH_SESSION`） | ✅ 已完成 | `SecurityConfig.cookieSerializer` |
| 退出登录清理服务端 Session 与本地状态 | ✅ 已完成 | `AuthController.logout`、`stores/auth.ts` |
| 生产环境 `SESSION_COOKIE_SECURE=true` | ⬜ 待部署时配置 | 由环境变量控制，staging/生产必须开启 |

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
| 开发默认密钥不得用于生产 | ⬜ 待部署时替换 | 由 `FIELD_ENCRYPTION_KEY`/`FIELD_HMAC_KEY` 环境变量注入 |
| 生产启用 KMS 与密钥轮换 | ⬜ Phase 2 | 对应 Task 22 |

## 6. 传输与部署

| 检查项 | 状态 | 证据 |
|---|---|---|
| 生产强制 HTTPS | ⬜ 待部署 | 对应 Task 18 Nginx 配置 |
| 安全响应头（HSTS、X-Content-Type-Options 等） | ⬜ 待部署 | 对应 Task 18 |
| 数据库不暴露公网端口 | ⬜ 待部署 | 对应 Task 18 `compose.staging.yml` |
| 日志不记录密码/Token/完整敏感字段 | ✅ 已完成 | `GlobalExceptionHandler` 仅记录 traceId 与异常 |

## 7. 结论

- **MVP 代码级安全测试全部通过（11 个用例）**。
- 待部署阶段完成的项目（HTTPS、安全头、生产密钥、KMS）已在第 5、6 节标注，对应 Task 18、22、25。
