# 审计日志模型设计

## 1. 目标

`audit_log` 用于记录高风险操作和关键配置变更，覆盖以下主线能力：
- 站点配置变更
- 页面发布与回滚
- 斗篷规则变更
- 支付通道与站点支付绑定变更
- 用户状态变更
- 订单状态手工修改

本设计对应计划项 `M11-06`。

---

## 2. 为什么需要单独审计模型

普通操作日志关注“做过什么”。

审计日志关注：
- 谁改了什么
- 改前是什么
- 改后是什么
- 何时改的
- 对哪个租户、哪个业务对象生效

对本项目来说，以下能力必须进入审计：
- 页面发布与版本回滚
- 斗篷规则启停和灰度
- 支付配置修改
- 站点禁用、归档、域名变更
- 用户禁用、角色调整

---

## 3. 表结构

表：`audit_log`

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户隔离 |
| `operator_user_id` | 操作人 |
| `operator_username` | 操作人用户名快照 |
| `module_code` | 模块，如 `SITE/PAGE/CLOAK/PAYMENT/USER` |
| `action_code` | 行为，如 `UPDATE/PUBLISH/ROLLBACK/DISABLE/BIND` |
| `target_type` | 目标对象类型 |
| `target_id` | 目标对象 ID |
| `target_name` | 目标对象名称快照 |
| `request_id` | 请求追踪 ID |
| `before_json` | 变更前摘要 |
| `after_json` | 变更后摘要 |
| `risk_level` | 风险等级 |
| `ip_address` | 操作来源 IP |
| `user_agent` | 客户端标识 |
| `remark` | 附加说明 |
| `created_at` | 创建时间 |

索引建议：
- `idx_audit_log_tenant_module_created (tenant_id, module_code, created_at)`
- `idx_audit_log_target (tenant_id, target_type, target_id, created_at)`
- `idx_audit_log_operator (tenant_id, operator_user_id, created_at)`

---

## 4. 审计事件范围

## 4.1 必须审计

| 模块 | 事件 |
|---|---|
| `USER` | 启用、禁用、重置密码、角色变更 |
| `SITE` | 编辑站点、归档、禁用、域名绑定变更 |
| `LAYOUT` | 页面发布、版本回滚、主题变量变更 |
| `CLOAK` | 规则创建、修改、启停、灰度发布 |
| `PAYMENT` | 通道配置、站点绑定、回调密钥变更 |
| `ORDER` | 手工改订单状态、退款、发货修正 |
| `ERP` | 连接器配置变更 |

## 4.2 暂不审计

以下暂不进入 `audit_log`：
- 普通列表查询
- 普通详情查看
- Dashboard 浏览行为
- 非关键字段的低风险查询参数变化

---

## 5. 数据记录原则

记录原则：
- 审计日志按“摘要”记录，不直接 dump 整个对象
- `before_json` / `after_json` 只保留关键信息
- 敏感信息必须脱敏

必须脱敏：
- 应用密码
- 支付密钥
- JWT
- 手机号、邮箱的敏感部分
- 客户完整地址、完整卡号、支付原始报文

建议脱敏方式：
- 密钥只保留前 4 位和后 2 位
- 邮箱按 `ab***@domain.com`
- 手机号按 `138****1234`

---

## 6. 风险等级

建议风险等级：

| 等级 | 说明 |
|---|---|
| `LOW` | 一般配置修改 |
| `MEDIUM` | 影响单站点运营配置 |
| `HIGH` | 影响支付、斗篷、发布或订单状态 |
| `CRITICAL` | 涉及资金、全站可用性或批量变更 |

示例：
- 用户昵称修改：`LOW`
- 站点域名变更：`MEDIUM`
- 页面发布、斗篷规则启停：`HIGH`
- 支付回调密钥修改：`CRITICAL`

---

## 7. 接入策略

接入顺序：
1. 用户启停
2. 站点编辑/归档
3. 页面发布与回滚
4. 斗篷规则变更
5. 支付配置变更

接入方式：
- 在 Application Service 完成核心业务变更后写入审计日志
- 保持“主事务成功后记录审计”原则
- 后续可升级为事件总线或异步持久化

---

## 8. 本轮落地范围

本轮落地内容：
- 定义 `audit_log` 表结构
- 定义审计事件范围
- 定义脱敏和风险等级规则
- 提供迁移脚本
- 新增后端审计域基础骨架

本轮暂不做：
- 审计拦截器
- 通用请求追踪 ID 注入
- 审计查询页面
- 所有业务接口的全量接入

