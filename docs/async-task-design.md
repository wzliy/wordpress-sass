# 统一异步任务模型设计

## 1. 目标

`async_task` 用于统一承接以下异步流程：
- 建站任务
- 模板初始化
- 域名验证
- SSL 签发与续期
- 斗篷配置下发
- ERP 推单 / 拉单
- 支付回调补偿
- 支付状态同步

本设计对应计划项 `M11-05`。

---

## 2. 为什么要从 `publish` 独立出来

当前仓库只有 `publishAsyncExecutor`，它只服务文章发布，存在三个问题：
- 任务模型绑定在 `post_publish`，无法复用于建站、支付、ERP、斗篷
- 状态机和重试规则散落在模块内部，平台无法统一监控
- 后续新能力若各自实现线程池和任务表，会导致补偿、告警、审计口径全部分裂

因此：
- `post_publish` 保持兼容用途
- 平台新增 `async_task` 作为主线任务中心
- `publish` 后续也应逐步迁移到统一任务模型，或至少复用统一执行框架

---

## 3. 任务边界

## 3.1 适合进入 `async_task` 的任务

适用场景：
- 执行时间不可忽略
- 依赖外部系统
- 需要重试、补偿、告警
- 需要查看执行历史和失败原因

例子：
- Multisite 建站
- 默认页面骨架初始化
- DNS 检测
- SSL 申请
- ERP 推送订单
- 支付回调重放

## 3.2 不适合进入 `async_task` 的任务

不适用场景：
- 纯内存计算
- 请求内必须同步完成且极短的逻辑
- 不需要重试和审计的轻量行为

例子：
- JWT 解析
- 列表查询
- 斗篷实时命中判定

---

## 4. 表结构

表：`async_task`

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户隔离 |
| `task_type` | 任务类型 |
| `biz_type` | 业务域，如 `SITE/PAYMENT/ERP/CLOAK` |
| `biz_id` | 业务主键，如站点 ID、支付单 ID |
| `idempotency_key` | 幂等键 |
| `status` | 任务状态 |
| `priority` | 优先级 |
| `payload_json` | 执行输入 |
| `result_json` | 执行结果摘要 |
| `error_message` | 最近失败原因 |
| `retry_count` | 已重试次数 |
| `max_retry_count` | 最大重试次数 |
| `next_run_at` | 下次可执行时间 |
| `locked_by` | 当前 worker 标识 |
| `locked_at` | 抢占时间 |
| `started_at` | 首次开始执行时间 |
| `finished_at` | 结束时间 |
| `created_by` | 创建人 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

索引建议：
- `idx_async_task_tenant_status_next_run (tenant_id, status, next_run_at)`
- `idx_async_task_biz (tenant_id, biz_type, biz_id)`
- `uk_async_task_idempotency (tenant_id, idempotency_key)`

---

## 5. 状态机

状态定义：

| 状态 | 含义 | 终态 |
|---|---|---|
| `PENDING` | 已创建，待调度 | 否 |
| `RUNNING` | 已被 worker 抢占并执行 | 否 |
| `RETRY_WAIT` | 执行失败，等待下一次重试 | 否 |
| `SUCCESS` | 执行成功 | 是 |
| `FAILED` | 达到最大重试次数，最终失败 | 是 |
| `CANCELED` | 被人工或系统取消 | 是 |

状态流转：

```text
PENDING -> RUNNING -> SUCCESS
PENDING -> RUNNING -> RETRY_WAIT -> RUNNING -> SUCCESS
PENDING -> RUNNING -> RETRY_WAIT -> RUNNING -> FAILED
PENDING/RETRY_WAIT -> CANCELED
```

---

## 6. 任务类型建议

首批任务类型：
- `SITE_PROVISION`
- `SITE_TEMPLATE_INIT`
- `DOMAIN_VERIFY`
- `SSL_APPLY`
- `SSL_RENEW`
- `CLOAK_DEPLOY`
- `PAYMENT_CALLBACK_RETRY`
- `PAYMENT_STATUS_SYNC`
- `ERP_PUSH_ORDER`
- `ERP_PULL_ORDER`
- `ERP_PUSH_PRODUCT`
- `ERP_PULL_INVENTORY`
- `LEGACY_POST_PUBLISH`

其中：
- `LEGACY_POST_PUBLISH` 只作为过渡类型，便于后续兼容迁移

---

## 7. 执行规则

创建规则：
- 所有任务创建时必须写入 `tenant_id`
- 所有任务必须具备 `idempotency_key`
- 默认状态为 `PENDING`

抢占规则：
- worker 只抢占 `PENDING` 或 `RETRY_WAIT` 且 `next_run_at <= now()` 的任务
- 抢占时写入 `locked_by` 和 `locked_at`

重试规则：
- 可重试错误进入 `RETRY_WAIT`
- 默认退避策略先采用固定阶梯：`1m / 5m / 15m`
- 非重试错误直接进入 `FAILED`

审计规则：
- 关键输入保存在 `payload_json`
- 关键输出摘要保存在 `result_json`
- 敏感信息不得原样写入 `payload_json`

---

## 8. 与现有发布模块的关系

当前阶段：
- `publishAsyncExecutor` 继续保留
- `post_publish` 继续承担兼容发布记录

下一阶段：
- 发布逻辑可在创建 `post_publish` 的同时创建一条 `async_task`
- 也可以直接由统一任务 worker 调用 `publish` 执行器
- 但不再继续为其他新模块复制 `publishAsyncExecutor` 模式

---

## 9. 本轮落地范围

本轮落地内容：
- 定义 `async_task` 表结构
- 定义任务状态机
- 定义首批任务类型
- 提供迁移脚本
- 新增后端任务域基础骨架

本轮暂不做：
- 通用 worker 调度器实现
- 定时扫描与分布式抢占
- 任务管理后台页面
- 统一任务重试服务

