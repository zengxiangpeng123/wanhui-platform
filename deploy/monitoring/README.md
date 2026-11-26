# 万汇网监控与告警

| 项目 | 内容 |
|---|---|
| 文档编号 | OPS-MONITOR-001 |
| 版本 | 1.0 |
| 更新日期 | 2026-11-26 |
| 对应任务 | Task 24 监控、日志与告警 |

## 1. 暴露端点

| 端点 | 用途 |
|---|---|
| `GET /actuator/health/liveness` | 进程存活探针（容器 liveness） |
| `GET /actuator/health/readiness` | 依赖就绪探针（容器 readiness，含数据库） |
| `GET /actuator/metrics` | Micrometer 指标列表 |
| `GET /actuator/prometheus` | Prometheus 抓取格式 |
| `GET /actuator/info` | 应用信息 |

生产环境建议仅在内网暴露 `/actuator/**`，由 Nginx 限制来源或使用独立管理端口。

## 2. Prometheus 抓取示例

```yaml
scrape_configs:
  - job_name: wanhui-backend
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['app:8080']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
```

## 3. 告警阈值

| 告警 | 指标 | 阈值 | 级别 | 处置 |
|---|---|---|---|---|
| 错误率高 | `http_server_requests_seconds_count{status=~"5.."}` 占比 | 5 分钟内 > 5% | Warning | 查看 `traceId` 与异常日志 |
| 错误率严重 | 同上 | 5 分钟内 > 10% | Critical | 触发回滚评估 |
| P95 延迟 | `http_server_requests_seconds{quantile="0.95"}` | 10 分钟内 > 1s | Warning | 检查 DB 慢查询与连接池 |
| P95 延迟严重 | 同上 | 10 分钟内 > 2s | Critical | 限流/扩容/回滚 |
| 应用不可用 | `up{job="wanhui-backend"} == 0` | 持续 2 分钟 | Critical | 重启容器，检查日志 |
| MySQL 不可用 | readiness 探针失败 | 持续 1 分钟 | Critical | 检查磁盘、连接数、备份恢复 |
| 磁盘空间 | 节点磁盘使用率 | > 80% Warning / > 90% Critical | — | 清理旧备份与日志 |
| 备份失败 | 备份任务退出码非 0 或校验失败 | 任意一次 | Critical | 立即重跑并人工验证 |
| 证书过期 | TLS 证书剩余天数 | < 14 天 Warning / < 3 天 Critical | — | 更换证书并重启 Nginx |
| 限流突增 | `http_server_requests_seconds_count{status="429"}` | 10 分钟内 > 100 | Info | 排查暴力破解或异常客户端 |

## 4. 日志字段

结构化日志（ECS 格式）包含：

- `@timestamp`、`log.level`、`message`
- `traceId`（MDC，对应响应头 `X-Trace-Id`）
- 请求日志字段：`method`、`path`、`status`、`durationMs`

禁止出现：密码、Token、完整邮箱/手机号、密钥、请求体明文。

## 5. Grafana 建议面板

1. 请求量 / 错误率（按 status 分组）
2. P50 / P95 / P99 延迟
3. JVM 堆与非堆内存
4. 数据库连接池活跃/空闲
5. 容器重启次数与就绪状态
6. 备份任务成功/失败

## 6. 验证记录

- `/actuator/health/liveness`、`/actuator/health/readiness` 返回 `200 UP`（`HealthEndpointTest`）
- `/actuator/prometheus` 返回 `200` 且包含 JVM 指标（`HealthEndpointTest`）
- 全量后端测试通过，结构化日志不影响既有断言
