# Publish Module Design

## Goal
发布模块负责将平台中的文章投递到一个或多个 WordPress 站点，并对每个站点分别记录执行结果、重试状态和远端返回信息。

---

## Core Table

表：`post_publish`

一条记录表示：
- 一个租户下
- 一篇文章
- 向一个站点
- 发起的一次发布任务

推荐字段：

| Field | Meaning |
|---|---|
| `tenant_id` | 租户隔离 |
| `post_id` | 本地文章 ID |
| `site_id` | 目标站点 ID |
| `idempotency_key` | 幂等键，避免重复投递 |
| `publish_status` | 当前发布状态 |
| `target_status` | 投递到 WordPress 的目标状态，如 `publish` / `draft` |
| `last_http_status` | 最近一次 HTTP 返回码 |
| `remote_post_id` | 远端 WordPress 文章 ID |
| `remote_post_url` | 远端文章地址 |
| `error_message` | 最近一次失败原因 |
| `response_body` | 最近一次响应体摘要 |
| `retry_count` | 当前已重试次数 |
| `max_retry_count` | 最大重试次数，默认 3 |
| `next_retry_at` | 下一次重试时间 |
| `started_at` | 开始执行时间 |
| `finished_at` | 最终完成时间 |
| `created_at` | 创建时间 |

---

## Status Machine

`publish_status` 建议固定为以下状态：

| Status | Meaning | Terminal |
|---|---|---|
| `PENDING` | 已创建发布任务，等待执行 | No |
| `PROCESSING` | 正在调用 WordPress API | No |
| `RETRY_WAIT` | 本次失败，等待下一次重试 | No |
| `SUCCESS` | 发布成功 | Yes |
| `FAILED` | 已达到最大重试次数，最终失败 | Yes |
| `CANCELED` | 人工取消或因业务原因终止 | Yes |

---

## Transitions

标准流转：

1. 创建发布任务
`PENDING`

2. Worker 开始处理
`PENDING -> PROCESSING`

3. 发布成功
`PROCESSING -> SUCCESS`

4. 可重试失败
`PROCESSING -> RETRY_WAIT`

5. 重试执行
`RETRY_WAIT -> PROCESSING`

6. 达到最大重试次数
`PROCESSING -> FAILED`

7. 人工取消
`PENDING/RETRY_WAIT -> CANCELED`

---

## Retry Rules

建议重试条件：
- HTTP 5xx
- 网络超时
- 连接失败
- WordPress 临时不可用

不建议重试：
- 401 鉴权失败
- 403 权限不足
- 404 目标 API 不存在
- 请求参数错误

默认规则：
- `max_retry_count = 3`
- 重试间隔可以先采用固定退避：`1m / 5m / 15m`

当前项目实现：
- 已实现异步 worker 执行
- `POST /publish` 只负责创建 `PENDING` 记录并触发后台执行
- worker 会将任务更新为 `PROCESSING / SUCCESS / FAILED`
- 当前仍采用“立即重试最多 3 次”的简化策略

---

## Idempotency

为了避免重复发布，建议幂等键按以下方式生成：

`tenantId:postId:siteId:targetStatus`

如果未来支持“重新发布”或“强制覆盖发布”，再引入版本号或批次号。

---

## MVP Execution Flow

### Create

`POST /publish`

输入：
- `postId`
- `siteIds`

服务端行为：
- 校验文章归属和租户归属
- 校验站点是否属于当前租户
- 为每个 `siteId` 生成一条 `PENDING` 记录
- 幂等冲突时可直接复用已有未终态记录

### Execute

Worker 或同步服务执行：
- 查询 `PENDING / RETRY_WAIT`
- 更新为 `PROCESSING`
- 调用 WordPress `POST /wp-json/wp/v2/posts`
- 成功写入 `remote_post_id / remote_post_url / response_body`
- 失败根据错误类型进入 `RETRY_WAIT` 或 `FAILED`

---

## API Suggestions

后续建议补这几个接口：

- `POST /publish`
- `GET /publish/list`
- `GET /publish/detail?id=...`
- `POST /publish/retry`
- `POST /publish/cancel`

---

## Notes

当前已经切到异步 worker 执行，后续如果再接真正消息队列，也不需要推翻这套表结构和流转规则。
