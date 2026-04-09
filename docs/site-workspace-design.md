# 站点工作台信息架构与接口聚合设计

## 1. 目标

站点工作台用于把“站点概览、页面入口、支付、斗篷、订单、任务、告警”收敛到单一入口，替代当前仅能做接入、建站和连通性测试的站点列表页。

本设计对应计划项 `M12-01`。

工作台的核心目标：
- 让用户进入单个站点后，先看到是否可运营，而不是先看到一张原始字段表
- 为后续页面装修、支付、斗篷、订单、域名等模块提供统一站点上下文
- 在主链路模块尚未全部落地前，先定义稳定的聚合 DTO 和缺省占位规则，避免前后端反复返工

---

## 2. 为什么需要从站点列表升级为工作台

当前 `site` 模块已有能力：
- 站点接入 `/site/register`
- 自动建站 `/site/provision`
- 站点列表 `/site/list`
- 连通性测试 `/site/test`

但当前模型仍停留在“WordPress 站点接入记录”层，存在以下缺口：
- 缺少单站点运营入口，用户无法围绕一个站点继续做页面、支付、斗篷和订单操作
- `site` 列表字段只覆盖基础地址和账号信息，不能表达模板、地区、语言、币种、模块准备状态
- 后续若各模块自己定义首页卡片和摘要，会导致同一站点信息在多个页面重复查询和重复拼装

因此，`M12` 需要先把“站点工作台”定义成聚合层，而不是继续扩展 `site/list`。

---

## 3. 信息架构

## 3.1 页面定位

站点工作台是单站点的一级入口，路由建议：
- 前端：`/sites/:siteId/workspace`
- 后端：`GET /site/workspace?id={siteId}`

进入工作台后，用户应该在首屏回答 4 个问题：
- 这是哪个站点
- 当前能不能正常运营
- 还有哪些配置没补齐
- 下一步最该点哪里

## 3.2 页面区块

建议页面按以下顺序组织：

1. 站点头部
- 站点名称、域名、站点状态
- 模板、国家、语言、币种
- 最近更新时间
- 快捷动作：打开站点、进入装修、配置支付、配置斗篷、查看订单

2. 运营状态横幅
- 建站中、失败、待支付配置、待域名绑定、存在高风险告警时优先展示
- 展示 1 条主状态 + 1 到 3 条操作建议

3. 核心概览区
- 页面概览
- 支付概览
- 斗篷概览
- 订单概览

4. 待处理事项区
- 异步任务
- 风险告警
- 初始化缺项

5. 最近活动区
- 最近建站事件
- 最近发布事件
- 最近配置变更
- 最近支付或斗篷异常

6. 基础配置区
- WordPress/站点访问入口
- 域名和 SSL 状态
- 模板来源
- 创建信息和租户隔离信息

## 3.3 模块卡片定义

### 页面概览卡

关注点：
- 已创建页面数
- 已发布页面数
- 最近发布时间
- 默认首页 / 结账页是否存在

主要动作：
- 进入页面装修
- 查看页面树
- 发布页面

### 支付概览卡

关注点：
- 是否绑定支付通道
- 默认通道名称
- 支付状态
- 近 7 日支付成功率
- 待处理回调异常数

主要动作：
- 绑定支付
- 查看支付单
- 查看异常

### 斗篷概览卡

关注点：
- 是否启用规则
- 生效规则数
- 灰度规则数
- 最近 24 小时命中率
- 最近异常日志数

主要动作：
- 管理规则
- 查看命中日志
- 紧急停用

### 订单概览卡

关注点：
- 今日订单数
- 近 7 日订单数
- 待支付 / 待发货 / 异常订单数
- 最近订单时间

主要动作：
- 查看订单列表
- 查看异常订单

---

## 4. 聚合模型设计

## 4.1 顶层 DTO

建议新增聚合响应模型 `SiteWorkspaceDto`：

| 字段 | 说明 |
|---|---|
| `siteId` | 站点 ID |
| `tenantId` | 租户 ID |
| `workspaceStatus` | 工作台状态，面向 UI |
| `generatedAt` | 聚合时间 |
| `profile` | 站点基础档案 |
| `readiness` | 可运营准备度 |
| `moduleSummaries` | 页面/支付/斗篷/订单等模块摘要 |
| `pendingTasks` | 当前待处理异步任务 |
| `alerts` | 风险告警 |
| `recentActivities` | 最近活动流 |
| `quickActions` | 当前建议动作 |

建议 `workspaceStatus`：
- `CREATING`
- `INITIALIZING`
- `ACTIVE`
- `ACTION_REQUIRED`
- `AT_RISK`
- `DISABLED`
- `ARCHIVED`

## 4.2 站点基础档案 `profile`

| 字段 | 说明 | 数据来源 |
|---|---|---|
| `siteId` | 站点 ID | `site.id` |
| `name` | 站点名称 | `site.name` |
| `siteType` | 站点类型 | `site.site_type` |
| `domain` | 主访问域名 | `site.domain` |
| `baseUrl` | 站点地址 | `site.base_url` |
| `adminUrl` | 后台地址 | `site.admin_url` |
| `status` | 站点状态 | `site.status` + 新状态映射 |
| `provisionStatus` | 建站状态 | `site.provision_status` |
| `statusMessage` | 状态说明 | `site.status_msg` |
| `templateCode` | 模板编码 | 未来 `site_template` / `site` 扩展字段 |
| `templateName` | 模板名称 | 未来 `site_template` |
| `countryCode` | 国家 | 未来 `site` 扩展字段 |
| `languageCode` | 默认语言 | 未来 `site` 扩展字段 |
| `currencyCode` | 默认币种 | 未来 `site` 扩展字段 |
| `createdAt` | 创建时间 | `site.created_at` |
| `createdBy` | 创建人 | 未来 `site.created_by` 或审计快照 |

## 4.3 可运营准备度 `readiness`

用于回答“站点现在是否可以对外运营”。

建议字段：

| 字段 | 说明 |
|---|---|
| `score` | 0 到 100 的准备度分数 |
| `level` | `NOT_READY / BASIC_READY / READY / RISK` |
| `items` | 检查项列表 |

检查项首批建议：
- 站点基础地址是否可访问
- 模板是否已绑定
- 首页/结账页是否已初始化
- 支付是否已绑定
- 斗篷是否已配置
- 域名/SSL 是否已配置

单项结构建议：

| 字段 | 说明 |
|---|---|
| `code` | 检查项编码，如 `PAYMENT_BOUND` |
| `label` | 显示名称 |
| `status` | `DONE / TODO / WARNING / BLOCKED` |
| `message` | 当前说明 |
| `action` | 建议动作，如 `GO_PAYMENT_BIND` |

## 4.4 模块摘要 `moduleSummaries`

建议按模块统一结构返回，避免前端为每个模块写一套判空规则。

通用结构：

| 字段 | 说明 |
|---|---|
| `module` | 模块编码，如 `PAGE / PAYMENT / CLOAK / ORDER / DOMAIN` |
| `status` | `READY / CONFIGURING / ACTION_REQUIRED / DISABLED / UNAVAILABLE` |
| `title` | 卡片标题 |
| `primaryMetric` | 主指标 |
| `secondaryMetrics` | 次级指标列表 |
| `highlights` | 重点摘要 |
| `actions` | 模块快捷动作 |

首批模块：
- `PAGE`
- `PAYMENT`
- `CLOAK`
- `ORDER`
- `DOMAIN`

其中 `DOMAIN` 不要求在 `M12-02` 完整实现，但应保留卡片占位，避免后续 UI 改版。

## 4.5 待处理任务 `pendingTasks`

数据来源优先使用 `async_task`。

建议字段：

| 字段 | 说明 |
|---|---|
| `taskId` | 任务 ID |
| `taskType` | 任务类型 |
| `status` | 任务状态 |
| `title` | UI 标题 |
| `message` | 当前说明 |
| `nextRunAt` | 下次执行时间 |
| `startedAt` | 开始时间 |
| `retryCount` | 已重试次数 |

首批重点任务：
- `SITE_PROVISION`
- `SITE_TEMPLATE_INIT`
- `DOMAIN_VERIFY`
- `SSL_APPLY`

## 4.6 告警 `alerts`

告警不等于任务，主要面向“需要人工注意”的问题。

建议字段：

| 字段 | 说明 |
|---|---|
| `level` | `INFO / WARNING / HIGH / CRITICAL` |
| `code` | 告警编码 |
| `title` | 标题 |
| `message` | 说明 |
| `action` | 建议动作 |
| `createdAt` | 产生时间 |

首批告警来源：
- 建站失败
- 站点连接测试失败
- 支付回调异常
- 斗篷规则异常停用

## 4.7 最近活动 `recentActivities`

建议以统一活动流返回，优先从 `audit_log` 和关键业务表聚合。

字段建议：

| 字段 | 说明 |
|---|---|
| `type` | 活动类型，如 `PROVISION / PAGE_PUBLISH / PAYMENT_BIND / CLOAK_UPDATE` |
| `title` | 活动标题 |
| `description` | 摘要说明 |
| `operatorName` | 操作人 |
| `occurredAt` | 发生时间 |
| `targetId` | 目标对象 ID |
| `targetType` | 目标对象类型 |

---

## 5. 接口设计

## 5.1 主接口

建议在 `M12-02` 实现：

```http
GET /site/workspace?id={siteId}
```

请求规则：
- 必须带 `siteId`
- 服务端从 `TenantContext` 取 `tenant_id`
- 必须校验 `site_id + tenant_id`

响应结构示例：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "siteId": 101,
    "tenantId": 1,
    "workspaceStatus": "ACTION_REQUIRED",
    "generatedAt": "2026-04-09T18:30:00",
    "profile": {
      "siteId": 101,
      "name": "UK Beauty Store",
      "siteType": "PROVISIONED",
      "domain": "https://uk-beauty.demo.local",
      "adminUrl": "https://uk-beauty.demo.local/wp-admin",
      "status": "ACTIVE",
      "provisionStatus": "ACTIVE",
      "templateCode": null,
      "templateName": null,
      "countryCode": null,
      "languageCode": null,
      "currencyCode": null
    },
    "readiness": {
      "score": 45,
      "level": "BASIC_READY",
      "items": [
        {
          "code": "SITE_ACCESSIBLE",
          "label": "站点可访问",
          "status": "DONE",
          "message": "最近一次连接测试成功",
          "action": "OPEN_SITE"
        },
        {
          "code": "PAYMENT_BOUND",
          "label": "支付已绑定",
          "status": "TODO",
          "message": "尚未配置默认支付通道",
          "action": "GO_PAYMENT_BIND"
        }
      ]
    },
    "moduleSummaries": [],
    "pendingTasks": [],
    "alerts": [],
    "recentActivities": [],
    "quickActions": []
  }
}
```

## 5.2 聚合策略

主接口采用“单接口聚合 + 模块可缺省”策略：
- 首屏只调一个接口，降低前端首屏编排复杂度
- 某个模块尚未落库时，返回占位状态，不返回假数据
- 后续各模块详情页再走独立接口

缺省规则：
- 模块未实现：`status = UNAVAILABLE`
- 模块已实现但站点未配置：`status = ACTION_REQUIRED`
- 模块已启用且可用：`status = READY`

## 5.3 推荐的后端分层

建议在 `site` 域新增以下对象：
- `site/application/SiteWorkspaceApplicationService`
- `site/application/dto/SiteWorkspaceDto`
- `site/application/dto/SiteWorkspaceModuleDto`
- `site/interfaces/SiteWorkspaceController`

聚合职责边界：
- `site` 域负责聚合和站点上下文校验
- 其他域负责提供各自摘要查询接口或 repository
- 工作台接口不直接承接编辑动作，只返回摘要和导航动作

---

## 6. 数据来源与占位策略

## 6.1 当前可直接复用的数据

当前仓库已具备，可直接用于 `M12-02` 第一版：
- `site`：站点名称、域名、后台地址、状态、建站状态、状态说明、创建时间
- `async_task`：待处理任务模型
- `audit_log`：最近活动流骨架

## 6.2 需要后续模块补齐的数据

| 模块 | 后续来源 | 对应计划项 |
|---|---|---|
| 模板 | `site_template`、`site.template_id` | `M12-03` |
| 页面 | `page`、`page_layout_version` | `M13-01` 到 `M13-04` |
| 斗篷 | `cloak_rule`、`cloak_hit_log` | `M14` |
| 支付 | `payment_channel`、`payment_order` | `M15-04` 到 `M15-05` |
| 订单 | `order`、`order_item` | `M15-01` 到 `M15-03` |
| 域名/SSL | `site_domain`、证书状态表 | `M16-01` 到 `M16-02` |

## 6.3 第一版落地原则

`M12-02` 不应等待所有主线模块全部完成。

第一版工作台允许：
- 页面、支付、斗篷、订单卡片先展示占位状态
- 只接入当前已有的站点档案、建站状态、异步任务和审计活动
- 通过统一状态码保证后续模块接入时前端不改结构

---

## 7. 与现有页面和任务的关系

对现有任务的影响：
- `M4-08` 站点编辑能力应收敛进工作台，而不是继续扩展列表页内编辑
- `M4-11` 站点详情页应直接并入工作台，不再单独设计一张传统详情页
- `M8-14` 站点列表重构应增加“进入工作台”主操作
- `M12-03` 模板中心的字段和接口必须兼容本设计中的 `templateCode/templateName`

对旧内容发布链路的处理：
- `post/publish` 继续保留为兼容模块
- 但工作台首屏不再把内容发布作为主入口

---

## 8. 本轮落地范围

本轮落地内容：
- 定义站点工作台页面信息架构
- 定义工作台主接口和聚合 DTO
- 定义模块摘要、任务、告警、活动流的统一结构
- 定义首版占位策略和后续模块接入边界

本轮暂不做：
- 工作台后端接口实现
- 工作台前端页面实现
- 模板中心表结构和接口实现
- 页面、支付、斗篷、订单模块真实统计计算
