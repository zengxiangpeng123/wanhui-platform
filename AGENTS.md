# 万汇网平台项目规则

本文件只作用于 `wanhui-platform` 仓库，不修改或替代全局 Codex/AGENTS 配置。

## 1. 基本原则

1. `main` 是稳定分支，只接收已经完成开发、测试和评审的改动。
2. 除仓库首次初始化外，不允许直接向 `main` 提交业务代码或文档改动。
3. 每个独立需求、修复、重构或文档任务使用独立分支。
4. 分支完成并通过测试后，才允许合入 `main` 并推送远端。
5. 测试不通过、权限不通过、存在真实密钥或数据库迁移未验证时，禁止合入 `main`。
6. 不删除或改写已有 Git 历史；不回退他人提交；不使用强制推送覆盖 `main`。

## 2. 分支命名

从最新 `origin/main` 创建分支，命名遵循：

```text
feat/<short-name>       新功能
fix/<short-name>        缺陷修复
refactor/<short-name>   重构
test/<short-name>       测试和测试基础设施
docs/<short-name>       文档
chore/<short-name>      工程配置、依赖、CI
hotfix/<short-name>     紧急线上修复
```

示例：

```text
feat/auth-session
feat/resource-favorite
fix/forum-soft-delete
refactor/api-response
test/auth-e2e
docs/environment-setup
chore/project-rules
```

## 3. 标准工作流

### 3.1 开始任务

```powershell
git fetch origin
git switch main
git pull --ff-only origin main
git switch -c feat/<short-name>
```

每个分支只处理一个明确目标。不要把多个不相关功能塞进同一个分支。

### 3.2 开发和测试

1. 先阅读 `docs/`、需求、接口和数据库设计。
2. 能写测试的改动必须先写失败测试，确认 RED。
3. 实现最小代码使测试变绿，必要时再重构。
4. 每个小步提交一次，提交信息使用：

```text
feat: ...
fix: ...
refactor: ...
test: ...
docs: ...
chore: ...
```

5. 每次提交前检查 `git diff`，禁止提交 `.env`、密码、Token、KMS 密钥、数据库数据文件和构建产物。

### 3.3 分支完成

在推送和合并前运行适用检查：

```powershell
# 前端
cd frontend
npm ci
npm run test:unit:run
npm run build
npm audit --omit=dev --audit-level=high

# 后端
cd ..\backend
$env:JAVA_HOME = 'C:\Users\15173\.jdks\ms-21.0.13.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -B -s ..\maven-settings.xml test

# 数据库相关改动
docker compose -f ..\deploy\compose.dev.yml config
docker compose -f ..\deploy\compose.dev.yml up -d --wait
docker compose -f ..\deploy\compose.dev.yml ps
```

Linux/macOS 后端命令：

```bash
cd backend
./mvnw -B -s ../maven-settings.xml test
```

涉及用户流程、权限、软删除、登录、收藏或评论的改动，还必须运行对应 Playwright E2E。

### 3.4 推送分支

测试通过后：

```powershell
git push -u origin <branch-name>
```

如果使用 Pull Request，必须等待 CI 和目标分支检查通过，再合并。

### 3.5 合入和推送 main

合并前重新同步：

```powershell
git fetch origin
git switch main
git pull --ff-only origin main
git merge --no-ff <branch-name>
```

在 `main` 上再次执行适用的完整测试。只有全部通过后才允许：

```powershell
git push origin main
```

不得为了“让远端更新”而跳过测试。不得直接 `git push --force origin main`。

## 4. 测试门禁

### 4.1 所有改动

- 变更内容与任务目标一致。
- 没有新增 `.env`、密钥、真实密码或生产配置。
- `git diff` 已人工检查。
- 相关文档已更新。

### 4.2 前端改动

- `npm ci` 成功。
- Vitest 单测通过。
- `npm run build` 通过。
- 生产依赖高危漏洞检查通过。
- 375px 宽度关键页面无横向溢出。

### 4.3 后端改动

- Maven Wrapper 使用 JDK 21。
- `mvnw test` 通过。
- API 权限和错误码符合 `011-API接口设计.md`。
- 涉及数据库时，空库迁移和重复执行验证通过。

### 4.4 安全改动

- 密码不以明文存储。
- 未登录 401、越权 403、CSRF 缺失 403。
- 管理员权限由服务端校验。
- 不将敏感字段完整写入日志。
- MySQL 不开放公网端口。

### 4.5 文档改动

- Markdown 链接、代码块和表格格式有效。
- 文档中的命令能够实际执行，或在文档中明确标记未验证。
- 需求、设计、API 或数据库变化同步更新追踪矩阵。

## 5. 数据库与迁移规则

1. 已合入并可能执行过的 Flyway 迁移禁止修改，只能新增版本。
2. 每个迁移必须在空库和已有数据的副本上验证。
3. 迁移不得依赖开发者手工建表或手工插入数据。
4. 删除列、改类型、拆分表必须有备份、回滚和兼容方案。
5. 业务表和 Session 表不得使用生产 root 账号连接。
6. 开发数据库密码和真实数据不得进入 Git。

## 6. 安全规则

1. 生产密码、JWT/Session 密钥、字段加密密钥只能通过 Docker Secrets 或云 KMS 注入。
2. 禁止将 `local_only_password` 或任何测试密码复制到生产配置。
3. 所有用户输入在服务端校验，输出时按纯文本安全渲染。
4. 外部 URL 仅允许 `http://` 和 `https://`。
5. 管理员操作必须审计。
6. 权限判断以服务端 Session/RBAC 为准，前端隐藏按钮不能作为安全边界。

## 7. 文档和目录

- 需求、产品、技术方案和任务计划以 `D:\dev\codex\me\wanhuiwang\docs` 为设计来源。
- 正式工程文件和运行说明放在本仓库的 `frontend/`、`backend/`、`deploy/`、`docs/`。
- 不在仓库根目录堆放临时脚本、截图或测试输出。
- 本地辅助产物进入 `logs/`、`uploads/` 或明确的 `docs/acceptance/` 证据目录，并遵守 `.gitignore`。

## 8. 例外处理

- 紧急线上问题使用 `hotfix/*`，仍需完成安全测试并记录理由。
- 只修改文档的分支可以运行较少的代码测试，但必须检查链接、格式和命令。
- 依赖升级必须单独分支，不能夹带业务功能。
- 任何无法执行的测试必须在合并说明中写明原因、影响和后续任务，不得伪报通过。

## 9. 合并检查清单

- [ ] 分支从最新 `origin/main` 创建。
- [ ] 任务范围单一且清晰。
- [ ] 失败测试和通过测试均有记录。
- [ ] 前端测试、构建和依赖审计通过。
- [ ] 后端测试和接口/权限测试通过。
- [ ] 数据库迁移已在空库验证。
- [ ] 没有密钥、真实数据或构建产物进入 Git。
- [ ] 已在 `main` 上复跑验证。
- [ ] 已推送到 `origin/main` 并确认远端提交一致。
