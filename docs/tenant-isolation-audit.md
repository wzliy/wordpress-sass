# 租户隔离审计记录

## 范围

本次审计对应计划项 `M11-04`，覆盖：
- 当前数据库表的 `tenant_id` 必填性
- 当前 Mapper 查询/更新语句是否带租户条件
- 当前兼容模块是否存在“按主键更新但未校验租户”的缺口

审计时间：
- 2026-04-09

---

## 现状结论

已具备的隔离基础：
- 鉴权拦截器会从 JWT 中解析 `tenantId` 并写入 `TenantContext`
- `site`、`post`、`publish list` 查询路径已按 `tenant_id` 过滤
- 业务服务层大多通过 `TenantContext` 或 `CurrentUser` 获取当前租户

发现的缺口：
- `user` 表相关更新 SQL 仅按 `id` 更新，未在 SQL 层追加 `tenant_id`
- `post_publish` 更新 SQL 仅按 `id` 更新，未在 SQL 层追加 `tenant_id`
- `docs/database.sql` 中多个业务表的 `tenant_id` 仍允许为空
- 缺少围绕 `tenant_id` 的常用索引，后续工作台、订单、支付查询会放大扫描成本

处理结果：
- 已将 `user` 更新语句改为 `WHERE id = ? AND tenant_id = ?`
- 已将 `post_publish` 更新语句改为 `WHERE id = ? AND tenant_id = ?`
- 已新增迁移脚本 `docs/migrations/20260409_harden_tenant_isolation.sql`
- 已在设计库脚本与测试 schema 中补充 `tenant_id` 非空约束和索引

---

## 当前表审计结果

| 表名 | 是否应包含 tenant_id | 当前处理 |
|---|---|---|
| `tenant` | 否 | 平台根表，保持不带 `tenant_id` |
| `plan` | 否 | 平台套餐表，保持不带 `tenant_id` |
| `user` | 是 | 已补 `NOT NULL` 和索引，更新 SQL 已带租户条件 |
| `site` | 是 | 已补 `NOT NULL` 和索引 |
| `post` | 是 | 已补 `NOT NULL` 和索引 |
| `post_publish` | 是 | 已补 `NOT NULL`、索引、幂等唯一索引，更新 SQL 已带租户条件 |
| `task` | 是 | 已补 `NOT NULL` 和索引，后续将被 `async_task` 替代 |

---

## 后续新增表强制规则

从本次审计完成后，新增业务表必须遵守：
- 只要是租户业务数据，就必须包含 `tenant_id BIGINT NOT NULL`
- 列表查询主路径必须至少有 `tenant_id` 前缀索引
- 详情更新和删除必须同时带 `id + tenant_id`
- 幂等键、业务唯一键优先设计为“`tenant_id + 业务键`”联合唯一
- 不允许通过服务层先校验租户、SQL 层却只按 `id` 更新

例外：
- 平台级配置表、租户根表、套餐表、系统字典表可以不带 `tenant_id`

---

## 对后续任务的约束

后续任务在落库时必须直接复用本审计规则：
- `M11-05`：`async_task` 默认带 `tenant_id`
- `M12`：站点模板、站点配置、站点工作台快照均需租户隔离
- `M13`：`page`、`page_layout_version`、`layout_block`、`theme_config` 均需租户隔离
- `M14`：`cloak_rule`、`cloak_hit_log` 需租户隔离
- `M15`：商品、订单、支付、ERP 全链路表必须租户隔离

