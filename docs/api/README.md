# 万汇网 API 使用说明

| 项目 | 内容 |
|---|---|
| 文档编号 | API-README-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-27 |
| API 版本 | `v1` |
| OpenAPI | 3.1 |
| 默认本地地址 | `http://127.0.0.1:8080` |

## 1. 入口与在线文档

| 地址 | 用途 |
|---|---|
| `/api/v1/*` | 业务 API |
| `/v3/api-docs` | OpenAPI JSON |
| `/swagger-ui/index.html` | Swagger UI |
| `/actuator/health/liveness` | 进程存活 |
| `/actuator/health/readiness` | 服务就绪 |
| `/actuator/prometheus` | Prometheus 指标 |

staging/生产通过同源 Nginx 访问 API：`https://<domain>/api/v1/*`。

## 2. 通用约定

### 2.1 内容类型

请求和响应默认使用 `application/json; charset=UTF-8`。分页参数使用 `page`（从 1 开始）和 `size`。

### 2.2 成功响应

```json
{
  "success": true,
  "data": {
    "items": [],
    "page": 1,
    "size": 20,
    "total": 0,
    "totalPages": 0
  }
}
```

资源分页还会返回 `sort`。没有响应数据的写操作通常返回 `data: null`。

### 2.3 错误响应

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "请求参数校验失败",
    "fieldErrors": [
      {"field": "title", "message": "标题不能为空"}
    ]
  },
  "traceId": "4f63c921d13e44c88349a95e7b91f801"
}
```

排查线上问题时提供 `traceId`，服务端日志可按该字段检索。不要记录或回传密码、Session、CSRF Token、完整邮箱或手机号。

### 2.4 常见状态码

| HTTP | 常见 `error.code` | 含义 |
|---|---|---|
| 400 | `INVALID_ARGUMENT` | JSON、类型或基础参数非法 |
| 401 | `AUTH_REQUIRED` | 未登录或会话失效 |
| 403 | `ACCESS_DENIED` | 无权限、CSRF 校验失败或角色不足 |
| 404 | `RESOURCE_NOT_FOUND` | 路径或对象不存在 |
| 405 | `METHOD_NOT_ALLOWED` | HTTP 方法不支持 |
| 409 | `CONFLICT` | 重复数据或状态冲突 |
| 410 | 帖子已删除相关业务码 | 已软删除，内容不可再访问 |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | Content-Type 不支持 |
| 422 | `VALIDATION_FAILED`、`INVALID_OLD_PASSWORD` | 字段校验或旧密码错误 |
| 429 | `RATE_LIMITED` | 登录、注册或评论触发限流 |
| 500 | `INTERNAL_ERROR` | 未预期服务端异常，不暴露堆栈 |

## 3. 认证、Session 与 CSRF

- 登录成功后服务端创建 Session，浏览器保存 `WH_SESSION` Cookie。
- `WH_SESSION` 为 `HttpOnly`、`SameSite=Lax`；生产环境额外启用 `Secure`。
- 前端不得把登录凭证写入 `localStorage`。
- 写请求使用 Cookie 中的 `XSRF-TOKEN`，并发送请求头 `X-XSRF-TOKEN`。
- 注册和登录接口按设计豁免 CSRF；其余需要认证的写操作必须携带 CSRF Token。
- 默认会话空闲超时为 30 分钟。

### 3.1 PowerShell 登录示例

```powershell
$base = 'http://127.0.0.1:8080/api/v1'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Invoke-RestMethod -Uri "$base/auth/csrf" -WebSession $session | Out-Null
$csrf = ($session.Cookies.GetCookies([uri]'http://127.0.0.1:8080') | Where-Object Name -eq 'XSRF-TOKEN').Value

$login = @{ account = 'demo_user'; password = 'Passw0rd!' } | ConvertTo-Json
Invoke-RestMethod `
  -Method Post `
  -Uri "$base/auth/login" `
  -WebSession $session `
  -ContentType 'application/json' `
  -Headers @{ 'X-XSRF-TOKEN' = $csrf } `
  -Body $login

Invoke-RestMethod -Uri "$base/auth/me" -WebSession $session
```

### 3.2 curl 示例

```bash
BASE=http://127.0.0.1:8080/api/v1
curl -sS -c cookies.txt "$BASE/auth/csrf"
CSRF=$(awk '$6 == "XSRF-TOKEN" {print $7}' cookies.txt)
curl -sS -b cookies.txt -c cookies.txt \
  -H "X-XSRF-TOKEN: $CSRF" \
  -H "Content-Type: application/json" \
  -d '{"account":"demo_user","password":"Passw0rd!"}' \
  "$BASE/auth/login"
curl -sS -b cookies.txt "$BASE/auth/me"
```

## 4. 端点清单

### 4.1 认证

| 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|
| GET | `/api/v1/auth/csrf` | 否 | 获取/刷新 CSRF Cookie |
| POST | `/api/v1/auth/register` | 否 | 注册账号 |
| POST | `/api/v1/auth/login` | 否 | 使用用户名或邮箱（`account`）登录并创建 Session |
| POST | `/api/v1/auth/logout` | 是 | 注销并清理 Session |
| GET | `/api/v1/auth/me` | 可选 | 返回当前登录用户概览 |

登录请求示例：

```json
{
  "account": "demo_user",
  "password": "Passw0rd!"
}
```

注册请求示例：

```json
{
  "username": "demo_user",
  "email": "demo@example.com",
  "password": "Passw0rd!"
}
```

### 4.2 资源目录

| 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|
| GET | `/api/v1/resources` | 否 | 分页、分类和关键词检索 |
| GET | `/api/v1/resources/{resourceKey}` | 否 | 通过稳定 `resourceKey` 获取详情 |
| PUT | `/api/v1/resources/{resourceKey}/favorite` | 是 | 收藏，幂等 |
| DELETE | `/api/v1/resources/{resourceKey}/favorite` | 是 | 取消收藏，幂等 |

列表参数：

| 参数 | 说明 |
|---|---|
| `category` | `design`、`ai`、`academic`、`media` |
| `q` | 名称、描述或标签关键词 |
| `page` | 页码，从 1 开始 |
| `size` | 每页数量 |

### 4.3 论坛

| 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|
| GET | `/api/v1/forum/categories` | 否 | 帖子分类 |
| GET | `/api/v1/forum/posts` | 否 | 帖子列表 |
| GET | `/api/v1/forum/posts/search?q=...` | 否 | 标题、正文和标签搜索 |
| GET | `/api/v1/forum/posts/{postId}` | 否 | 帖子详情；软删除返回 410 |
| POST | `/api/v1/forum/posts` | 是 | 发帖 |
| DELETE | `/api/v1/forum/posts/{postId}` | 是 | 作者或管理员软删除 |
| GET | `/api/v1/forum/posts/{postId}/comments` | 否 | 评论分页 |
| POST | `/api/v1/forum/posts/{postId}/comments` | 是 | 发表评论 |
| PUT | `/api/v1/forum/posts/{postId}/like` | 是 | 点赞帖子，幂等 |
| DELETE | `/api/v1/forum/posts/{postId}/like` | 是 | 取消点赞，幂等 |
| PUT | `/api/v1/forum/posts/{postId}/favorite` | 是 | 收藏帖子，幂等 |
| DELETE | `/api/v1/forum/posts/{postId}/favorite` | 是 | 取消收藏，幂等 |
| PUT | `/api/v1/forum/comments/{commentId}/like` | 是 | 点赞评论，幂等 |
| DELETE | `/api/v1/forum/comments/{commentId}/like` | 是 | 取消评论点赞，幂等 |

发帖请求字段：

```json
{
  "categoryId": 1,
  "title": "示例标题",
  "content": "纯文本正文"
}
```

标题、正文、评论均按纯文本输出，前端不得使用未净化的 `v-html`。

### 4.4 用户中心

| 方法 | 路径 | 登录 | 说明 |
|---|---|---|---|
| GET | `/api/v1/users/me/profile` | 是 | 获取资料；邮箱/手机号仅返回掩码 |
| PATCH | `/api/v1/users/me/profile` | 是 | 修改昵称等允许编辑的字段 |
| POST | `/api/v1/users/me/password` | 是 | 校验旧密码后修改密码 |
| GET | `/api/v1/users/me/favorites/resources` | 是 | 收藏的资源 |
| GET | `/api/v1/users/me/recent-visits` | 是 | 最近访问 |
| POST | `/api/v1/users/me/recent-visits` | 是 | 记录访问 |
| DELETE | `/api/v1/users/me/recent-visits` | 是 | 清空最近访问 |
| GET | `/api/v1/users/me/sites` | 是 | 自定义网站列表 |
| POST | `/api/v1/users/me/sites` | 是 | 新增自定义网站 |
| DELETE | `/api/v1/users/me/sites/{siteId}` | 是 | 删除自定义网站 |

自定义网站 URL 只允许 `http` 或 `https`，服务端拒绝 `javascript:` 等危险协议。

### 4.5 管理接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/v1/admin/posts` | 管理员 | 管理端帖子列表 |
| DELETE | `/api/v1/admin/posts/{postId}` | 管理员 | 软删除并写审计日志 |
| GET | `/api/v1/admin/audit-logs` | 管理员 | 审计日志分页 |

普通用户访问管理接口返回 403。管理员删帖必须产生 `AuditLog`。

## 5. 限流

| 操作 | 默认容量 | 窗口 |
|---|---|---|
| 登录 | 10 次 | 60 秒 |
| 注册 | 5 次 | 60 秒 |
| 评论 | 20 次 | 60 秒 |

超限返回 HTTP 429：

```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMITED",
    "message": "请求过于频繁，请稍后再试"
  },
  "traceId": "..."
}
```

限流只在自动化 E2E 的专用运行环境中通过 `WANHUI_SECURITY_RATE_LIMIT_ENABLED=false` 临时关闭；安全测试必须单独覆盖限流。

## 6. 约定与兼容性

- 前端只依赖 DTO 字段，不直接依赖数据库实体。
- 资源收藏关联稳定 `resourceKey`，不关联展示名称。
- 帖子软删除后列表隐藏，详情返回 410。
- 收藏、点赞和取消操作应幂等。
- API 变更必须先更新 OpenAPI 契约测试，再修改实现。
- 对客户端可见的破坏性变更必须升级 API 版本，不得直接改变 `/api/v1` 语义。

## 7. 已知限制

- OpenAPI 描述当前随运行服务生成，尚未单独导出静态 schema 文件。
- 手机/微信登录、密码找回、举报和通知 API 不在当前范围。
- 真实第三方 KMS 接入由部署环境提供；仓库提供兼容的密钥版本与轮换能力。
