# 万汇网平台

万汇网前端、后端、数据库与部署工程。当前处于环境搭建阶段，尚未实现正式业务功能。

## 技术栈

- 前端：Vue 3 + TypeScript + Vite + Element Plus
- 后端：Spring Boot 4.2 + Java 21 + MyBatis-Plus Boot 4 Starter
- 数据库：MySQL 8.0 Docker

## 前置条件

- JDK 21
- Node.js 24 LTS 或团队确认的 LTS
- Docker Desktop / Docker Engine
- 项目自带 Maven Wrapper，不要求全局 Maven

本机默认 Java 可能是 17，执行后端命令前必须显式切换 JDK 21。

## 目录

```text
frontend/   Vue SPA
backend/    Spring Boot 应用
deploy/     Docker Compose 和部署脚本
docs/       工程级文档
```

## 本地启动

### 1. 选择 JDK 21

```powershell
$env:JAVA_HOME = 'C:\Users\15173\.jdks\ms-21.0.13.1'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

### 2. 启动 MySQL

```powershell
docker compose -f deploy/compose.dev.yml up -d --wait
docker compose -f deploy/compose.dev.yml ps
```

### 3. 启动后端

```powershell
cd backend
.\mvnw.cmd -s ..\maven-settings.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

后端默认地址：`http://127.0.0.1:8080`。

### 4. 启动前端

新开终端：

```powershell
cd frontend
npm ci
npm run dev
```

前端默认地址：`http://127.0.0.1:5173`，`/api` 会代理到后端。

## 验证

```powershell
cd frontend
npm run test:unit:run
npm run build

cd ..\backend
.\mvnw.cmd -s ..\maven-settings.xml test
```

## 环境变量

复制 `.env.example` 为本地 `.env` 并替换本地值。不要提交 `.env`、密码或生产密钥。

## 当前已知情况

- 前端 smoke test 和生产构建已通过。
- 后端 Spring Boot 4.2 上下文 smoke test 已通过。
- Docker MySQL 已启动并通过 healthcheck。
- 业务表、认证、资源、论坛和用户中心尚未实现。
- Element Plus 当前为全量引入，后续需要按需引入和路由拆包。

## 文档

工程级环境说明位于上级：

`D:\dev\codex\me\wanhuiwang\docs\05-环境搭建`

## 安全提醒

本地密码仅用于 Docker 开发环境。生产环境必须使用 Docker Secrets 或云 KMS，并启用 HTTPS、字段加密、审计和加密备份。
