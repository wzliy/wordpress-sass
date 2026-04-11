# 斗篷引擎设计基线

## 1. 目标

本设计对应 `docs/plan.md` 中的 `BX-04`，用于把当前仍停留在工作台占位态的“斗篷引擎”收口为可执行的模块设计和任务拆分。

本轮目标不是直接上线真实流量斗篷，而是明确：
- 站点维度的斗篷规则应该如何建模
- 请求命中判定与日志记录如何接入当前模块化单体
- 哪些能力先做管理与仿真，哪些能力继续留在后续增强

上位约束：
- `docs/prod/01-prd.md` 明确“Full cloak implementation”不在 demo 范围内
- `docs/prod/04-runbook.md` 明确当前阶段不要“build actual cloaking logic”

结论：
- `BX-04` 首批只做规则模型、后台管理、仿真与日志骨架
- 真实线上拦截和复杂识别策略留到后续阶段

---

## 2. 当前基线与缺口

当前仓库已有可复用能力：
- `site` 已提供站点上下文、工作台入口和站点级模块摘要
- `storefront` 已承接公开访问入口和基于 `Host` 的站点解析
- `async_task` 设计已为“斗篷配置下发”预留任务模型
- `audit_log` 设计已把“斗篷规则启停”列为 `HIGH` 风险事件
- `tenant-isolation-audit` 已要求 `cloak_rule`、`cloak_hit_log` 强制租户隔离

当前缺口：
- 没有独立的 `cloak` 领域
- 没有斗篷规则主档、命中日志和站点范围接口
- 工作台中的斗篷模块仍是占位文案
- 公开请求链路没有可插拔的命中判定组件

---

## 3. 范围与非目标

### 3.1 本阶段范围

首批交付范围：
- 站点级斗篷规则模型
- 条件与结果的 JSON 合同
- 规则的创建、编辑、排序、启停
- 命中仿真接口
- 命中日志表和查询接口
- 工作台与后台管理页入口

### 3.2 本阶段非目标

当前明确不做：
- 真实广告审核绕行策略
- 复杂指纹、JS 挑战、行为分析
- 边缘节点或 CDN 级规则执行
- 外部风控或 IP 情报供应商接入
- 全自动线上流量切分

---

## 4. 设计原则

### 4.1 先做站点级规则，不做平台级大一统引擎

规则默认挂在 `site_id` 下，避免一开始引入平台级继承、模板和批量下发的复杂度。

### 4.2 先做 schema-driven 规则配置，不做脚本表达式

首版条件直接存为受控 `condition_json`，不允许用户写任意脚本。

原因：
- 降低线上请求路径的执行风险
- 更容易做校验、仿真和审计
- 与当前页面装修器的受控 schema 思路一致

### 4.3 请求路径只允许轻量判定

未来如果接入 storefront，请求内只做：
- 读取当前站点已启用规则
- 构造 `CloakContext`
- 顺序匹配
- 返回命中结果

不允许在请求内调用外部接口。

### 4.4 命中日志与审计分离

- `cloak_hit_log`：记录请求命中结果，面向运营查询
- `audit_log`：记录规则创建、修改、启停、排序等管理动作

两者职责不同，不应混用。

---

## 5. 模块边界

建议新增 `cloak` 领域：

| 模块 | 职责 |
|---|---|
| `cloak.interfaces` | 规则管理、仿真、日志查询接口 |
| `cloak.application` | 规则编排、排序、启停、仿真命中 |
| `cloak.domain` | 规则、条件、结果、命中日志、仓储接口 |
| `cloak.infrastructure` | MyBatis 仓储、JSON 序列化、上下文解析 |

现有模块协作：

| 模块 | 协作方式 |
|---|---|
| `site` | 提供站点上下文与工作台入口 |
| `storefront` | 后续作为请求入口接入 `CloakEvaluationService` |
| `task` | 后续承接规则下发、缓存刷新等异步任务 |
| `ops` | 记录规则变更的审计日志 |

---

## 6. 数据模型

## 6.1 `cloak_rule`

首版建议单表承载规则主档与已发布配置，不引入独立版本表。

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户隔离 |
| `site_id` | 站点 ID |
| `rule_name` | 规则名称 |
| `priority` | 顺序，值越小优先级越高 |
| `status` | `DRAFT / ACTIVE / DISABLED` |
| `match_mode` | `ALL / ANY` |
| `traffic_percentage` | 灰度比例，`0-100` |
| `condition_json` | 规则条件 JSON |
| `result_type` | `NORMAL_PAGE / REVIEW_PAGE / EXTERNAL_REDIRECT / REJECT` |
| `result_json` | 命中结果 JSON |
| `version_no` | 每次保存递增，便于审计和缓存刷新 |
| `created_by` | 创建人 |
| `created_at` | 创建时间 |
| `updated_at` | 更新时间 |

约束建议：
- `INDEX (tenant_id, site_id, status, priority)`
- `UNIQUE (tenant_id, site_id, rule_name)`

### 为什么首版不拆 `cloak_rule_version`

因为当前 demo 明确不做真实线上斗篷流量引擎，首版主要目标是：
- 规则模型落地
- 后台可配置
- 可仿真和看日志

版本化可以先交给：
- `audit_log`
- `version_no`

只有当后续需要“规则草稿 / 发布 / 回滚”时，再补独立版本表。

## 6.2 `cloak_hit_log`

建议字段：

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `tenant_id` | 租户隔离 |
| `site_id` | 站点 ID |
| `rule_id` | 命中的规则 ID，可为空表示走默认结果 |
| `decision` | 最终决策 |
| `request_id` | 请求标识 |
| `request_summary_json` | 脱敏后的请求摘要 |
| `matched_condition_json` | 命中条件摘要 |
| `created_at` | 命中时间 |

请求摘要建议只保留：
- 国家
- ASN
- Referer 域名
- UTM 摘要
- 设备类型
- 浏览器族
- 首次访问标记
- IP 脱敏值

不直接存原始敏感数据。

---

## 7. 条件与结果合同

## 7.1 `condition_json`

首版建议结构：

```json
[
  {
    "dimension": "COUNTRY",
    "operator": "IN",
    "values": ["US", "CA"]
  },
  {
    "dimension": "REFERER",
    "operator": "CONTAINS",
    "values": ["facebook.com"]
  }
]
```

首批支持维度：
- `COUNTRY`
- `IP_CIDR`
- `ASN`
- `REFERER`
- `UTM_SOURCE`
- `DEVICE_TYPE`
- `BROWSER`
- `FIRST_VISIT`
- `ALLOW_LIST`
- `BLOCK_LIST`

## 7.2 `result_json`

首版建议结构：

```json
{
  "pageKey": "HOME",
  "redirectUrl": null,
  "httpStatus": 404,
  "reason": "review-traffic"
}
```

结果类型约束：
- `NORMAL_PAGE`：忽略 `redirectUrl`，直接继续站点默认页面
- `REVIEW_PAGE`：先走固定审核页或指定 `pageKey`
- `EXTERNAL_REDIRECT`：必须提供 `redirectUrl`
- `REJECT`：默认返回 `404`，后续可扩到 `403`

---

## 8. 运行时策略

未来接入 storefront 时，建议流程：

1. `Host` 解析得到 `site`
2. 基于请求构造 `CloakContext`
3. 读取当前站点 `ACTIVE` 规则并按 `priority ASC` 排序
4. 按 `match_mode` 顺序匹配
5. 命中第一条规则后立即返回 `CloakDecision`
6. 未命中则走默认 `NORMAL_PAGE`
7. 将命中摘要写入 `cloak_hit_log`

注意：
- `docs/async-task-design.md` 已明确“斗篷实时命中判定”不应进入 `async_task`
- 命中日志可先同步落库，后续再抽成异步写入

---

## 9. 接口建议

首版建议接口：

- `GET /api/admin/sites/{siteId}/cloak/rules`
- `POST /api/admin/sites/{siteId}/cloak/rules`
- `PUT /api/admin/sites/{siteId}/cloak/rules/{ruleId}`
- `POST /api/admin/sites/{siteId}/cloak/rules/{ruleId}/enable`
- `POST /api/admin/sites/{siteId}/cloak/rules/{ruleId}/disable`
- `PUT /api/admin/sites/{siteId}/cloak/rules/reorder`
- `POST /api/admin/sites/{siteId}/cloak/simulations`
- `GET /api/admin/sites/{siteId}/cloak/hits`

接口分层：
- `rules`：管理规则主档
- `simulations`：输入请求上下文，返回命中结果但不真正拦截线上请求
- `hits`：查询命中日志

---

## 10. 工作台与前端集成

工作台现状：
- 斗篷卡片只有占位文案，没有真实入口

下一步原则：
- `GO_CLOAK` 必须跳转到真实后台路由
- 斗篷模块摘要至少要展示：
  - 已启用规则数
  - 今日命中量
  - 最近异常数
- 管理页优先做“规则列表 + 右侧编辑抽屉/面板”，不先做复杂可视化流程图

---

## 11. 审计与异步约束

审计：
- 规则创建、修改、排序、启停都记 `audit_log`
- 风险等级按 `HIGH` 处理

异步：
- 真实配置下发、缓存刷新、边缘节点同步等后续进入 `async_task`
- 当前阶段不实现真实下发，只预留任务类型和幂等键设计

---

## 12. 分阶段实施建议

### BX-04-01 设计基线
- 完成本文档
- 明确规则模型、仿真和日志边界

### BX-04-02 schema 与仓储骨架
- 新增 `cloak_rule`、`cloak_hit_log` 表
- 新增 domain / repository / mapper

### BX-04-03 管理接口
- 新增规则创建、更新、排序、启停接口
- 补站点范围租户隔离校验

### BX-04-04 仿真与日志
- 新增 `CloakEvaluationService`
- 新增仿真接口和命中日志查询接口
- 暂不接入真实 storefront 请求链路

### BX-04-05 工作台与管理页
- 接通 `GO_CLOAK`
- 新增后台规则列表和编辑页
- 展示规则概览和命中日志

---

## 13. 非目标

本阶段明确不做：
- 真实广告平台对抗策略
- 复杂规则 DSL
- 边缘缓存同步
- 自动代理池、指纹池、设备伪装
- 审核页和正常页的自动切换上线

当前阶段只要求：
- 规则模型落库
- 后台可配置
- 可仿真
- 可查日志
- 工作台不再停留在占位态
