# 万汇网工程文档

| 分类 | 文档 |
|---|---|
| API | [API 使用说明](./api/README.md) |
| 验收 | [验收证据索引](./acceptance/README.md) |
| 验收 | [MVP E2E 记录](./acceptance/2026-11-24-e2e-record.md) |
| 验收 | [Phase 2 安全验收](./acceptance/phase2-security.md) |
| 验收 | [性能验收](./acceptance/performance.md) |`n| 验收 | [Docker staging 部署验收](./acceptance/deployment.md) |
| 运维 | [运维手册](./operations/runbook.md) |
| 运维 | [生产发布检查表](./operations/production-checklist.md) |
| 运维 | [发布检查表](./operations/release-checklist.md) |
| 运维 | [备份恢复与回滚演练](./operations/drill-record-2026-11-26.md) |
| 内容 | [站点文案](./content/site-copy.md) |

## 文档约定

- 代码、API 和部署说明以仓库内文档为交付基线。
- 产品需求、交互、技术方案和任务矩阵位于项目上级 `docs/` 目录。
- 验收记录必须包含环境、数据量、命令、实测结果和结论。
- 文档中的命令必须可在仓库根目录或明确的工作目录执行。
- 生产密钥、Cookie、Token、完整邮箱/手机号和证书私钥不得写入文档。

## 快速入口

- 本地启动、测试与部署：[README](../README.md)
- API 与 CSRF：[docs/api/README.md](./api/README.md)
- 浏览器完整流程：[docs/acceptance/2026-11-24-e2e-record.md](./acceptance/2026-11-24-e2e-record.md)
- 上线、备份、恢复与回滚：[docs/operations/runbook.md](./operations/runbook.md)
