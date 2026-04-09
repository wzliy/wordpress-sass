# 建站 SaaS 重构基线方案

## 1. 目标

本文档用于落地 `M11-01`、`M11-02`、`M11-03`：
- 梳理目标领域边界
- 明确后端/前端包结构重组方案
- 定义从“内容发布中心”迁移到“建站 SaaS 中心”的兼容策略
- 输出核心表结构与迁移脚本清单

本方案以 [architecture.md](/Users/wangzhulin/IdeaProjects/wordpress-sass/docs/architecture.md) 和 [requirement-breakdown.md](/Users/wangzhulin/IdeaProjects/wordpress-sass/docs/requirement-breakdown.md) 为准。

---

## 2. 当前实现快照

### 2.1 后端现状

当前后端模块主要包括：
- `auth`：登录、JWT、默认租户与管理员初始化
- `user`：用户列表、创建、资料编辑、启停、改密
- `site`：站点登记、列表、连接测试、Multisite 建站
- `post`：文章创建与列表
- `publish`：文章向 WordPress 站点发布
- `common`：统一响应、异常处理、租户上下文、鉴权拦截器

现状判断：
- 已有“租户 + 账号 + 站点接入 + Multisite provisioner”基础
- `post/publish` 仍占据较强中心地位
- 尚无页面装修、斗篷、商品、订单、支付、ERP 的领域边界与持久化模型
- 异步能力只在 `publish` 内部存在，尚未上升为统一任务中心

### 2.2 前端现状

当前前端页面主要包括：
- Dashboard
- 站点列表 / 接入 / 建站
- 文章列表 / 创建
- 发布中心 / 发布历史
- 用户列表 / 创建 / 编辑 / 改密

现状判断：
- 后台壳和基础列表能力已经可用
- 站点工作台、一键建站流程、页面编辑器、支付/订单/斗篷配置页尚未建立
- 页面入口仍偏“内容发布型后台”，与新产品定位不一致

### 2.3 数据库现状

当前核心表：
- `tenant`
- `user`
- `plan`
- `site`
- `post`
- `post_publish`
- `task`

现状判断：
- `tenant / user / site` 可继续复用
- `post / post_publish / task` 只适合兼容阶段，不足以承载新主线
- 需要新增建站 SaaS 主线表，而不是继续扩展 `post_*`

---

## 3. 目标领域边界

## 3.1 领域划分

后续采用单体内分域方式组织，边界如下：

| 领域 | 职责 | 当前状态 | 处理策略 |
|---|---|---|---|
| Identity | 登录、JWT、账号、角色、权限、租户归属 | 已有 `auth` + `user` | 保留并逐步归并 |
| Tenant | 租户、套餐、额度、初始化 | 部分已有 | 保留并增强 |
| Site | 建站、站点工作台、模板、域名绑定、站点状态 | 部分已有 | 以当前 `site` 为基础重构 |
| Layout | 页面、版本、区块、主题配置、模板复用 | 缺失 | 新增主领域 |
| Cloak | 斗篷规则、命中判定、日志审计 | 缺失 | 新增主领域 |
| Catalog | 商品、SKU、库存、定价 | 缺失 | 新增主领域 |
| Order | 客户、订单、订单项、发货 | 缺失 | 新增主领域 |
| Payment | 支付通道、支付单、回调、状态机 | 缺失 | 新增主领域 |
| ERP | ERP 连接器、同步任务、同步日志 | 缺失 | 新增主领域 |
| Task | 异步任务、重试、补偿、执行记录 | 缺失 | 从 `publish` 内部能力上升为平台能力 |
| Ops | 审计日志、操作日志、统计口径、告警 | 薄弱 | 新增平台支撑域 |
| ContentLegacy | 文章与 WordPress 发文 | 已有 | 降级为兼容域 |

## 3.2 主线与兼容域

主线域：
- `Site`
- `Layout`
- `Catalog`
- `Order`
- `Payment`
- `Cloak`
- `ERP`
- `Task`

基础复用域：
- `Identity`
- `Tenant`
- `Ops`

兼容维护域：
- `ContentLegacy`

明确要求：
- 后续新增功能默认禁止继续挂到 `post` 或 `publish` 模块
- WordPress 发文只作为兼容能力保留，不再作为首页导航主路径

---

## 4. 目标包结构

## 4.1 后端包结构

后端根包维持 `com.wpss.wordpresssass`，按领域拆分：

```text
com.wpss.wordpresssass
├── common
│   ├── api
│   ├── auth
│   ├── config
│   ├── exception
│   ├── tenant
│   └── util
├── identity
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── tenant
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── site
│   ├── application
│   ├── domain
│   ├── infrastructure
│   │   ├── persistence
│   │   ├── provision
│   │   ├── dns
│   │   └── ssl
│   └── interfaces
├── layout
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── cloak
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── catalog
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── order
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── payment
│   ├── application
│   ├── domain
│   ├── infrastructure
│   │   └── gateway
│   └── interfaces
├── erp
│   ├── application
│   ├── domain
│   ├── infrastructure
│   │   └── connector
│   └── interfaces
├── task
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
├── ops
│   ├── application
│   ├── domain
│   ├── infrastructure
│   └── interfaces
└── legacy
    └── content
        ├── post
        └── publish
```

## 4.2 包结构约束

约束如下：
- `interfaces` 只放 Controller、request/response DTO、页面聚合接口
- `application` 只负责编排，不直接持有 MyBatis `Mapper`
- `domain` 只放领域模型、仓储接口、领域服务、状态机
- `infrastructure` 放 MyBatis 实现、外部适配器、数据对象
- 新模块统一按 `application / domain / infrastructure / interfaces` 四层组织
- 老模块 `auth`、`user`、`post`、`publish` 不再横向扩张

## 4.3 当前模块迁移映射

| 当前模块 | 目标模块 | 策略 |
|---|---|---|
| `auth` | `identity` + `tenant` | 分阶段迁移，先保持包名不动，新增代码进新包 |
| `user` | `identity` | 用户管理并入身份中心 |
| `site` | `site` | 保留模块名，内部按新子目录重整 |
| `post` | `legacy.content.post` | 降级为兼容域 |
| `publish` | `legacy.content.publish` + `task` | 发布任务调度能力抽到 `task` |
| `common` | `common` | 继续保留 |

迁移原则：
- 先新增目标包和新模块，再逐步迁移旧逻辑
- 不做“一次性大搬家”，避免当前功能全部失稳

---

## 5. 前端模块重组方案

目标前端目录：

```text
frontend/src
├── api
│   ├── identity.js
│   ├── site.js
│   ├── layout.js
│   ├── cloak.js
│   ├── catalog.js
│   ├── order.js
│   ├── payment.js
│   ├── erp.js
│   └── legacy-content.js
├── pages
│   ├── dashboard
│   ├── workspace
│   ├── site
│   ├── template
│   ├── layout
│   ├── cloak
│   ├── catalog
│   ├── order
│   ├── payment
│   ├── domain
│   ├── user
│   └── legacy-content
└── components
    ├── workspace
    ├── editor
    ├── chart
    └── common
```

前端导航调整原则：
- 一级导航从“文章/发布”切到“站点 / 装修 / 商品 / 订单 / 支付 / 斗篷”
- 发布中心进入“兼容工具”或“历史功能”分组
- 站点详情页升级为站点工作台，承接多模块入口

---

## 6. 兼容迁移策略

## 6.1 保留能力

以下能力继续复用：
- JWT 鉴权、`tenant_id` 注入、拦截器机制
- 用户管理与租户初始化
- WordPress Multisite provisioner
- `WpClient` 及远程站点连接测试

## 6.2 降级能力

以下能力降级为兼容域：
- 文章创建与文章列表
- 文章发布到 WordPress
- 发布历史查询

降级后的处理方式：
- 保留接口，避免已接前端或测试立即失效
- 从默认导航中下沉
- 不再为其扩展新的主业务模型

## 6.3 禁止事项

从本轮重构开始，禁止：
- 用 `post` 表承载页面装修内容
- 用 `post_publish` 表承载建站、支付、ERP 或斗篷任务
- 在 `site` 表继续塞入商品、订单、支付等跨域字段

## 6.4 API 兼容策略

策略如下：
- 旧接口保持可用：`/post/*`、`/publish/*`
- 新主线接口使用独立资源路径，例如：
  - `/site/workspace`
  - `/site/template/*`
  - `/layout/*`
  - `/cloak/*`
  - `/catalog/*`
  - `/order/*`
  - `/payment/*`
  - `/erp/*`
  - `/task/*`
- 新旧接口可以并行一段时间，但新页面禁止继续依赖旧发布模型

## 6.5 迁移顺序

推荐顺序：
1. 完成领域边界、兼容策略、DDL 清单
2. 建立 `async_task`、`audit_log`、站点模板与页面模型
3. 完成站点工作台与一键建站初始化增强
4. 落地页面编辑器 MVP
5. 落地支付、订单、商品主链路
6. 落地斗篷规则与日志
7. 对接 ERP 连接器
8. 最后再清理遗留内容发布入口

---

## 7. 核心表结构清单

## 7.1 继续复用/扩展的表

| 表名 | 策略 |
|---|---|
| `tenant` | 保留，后续补额度与套餐关联字段 |
| `user` | 保留，后续补手机号、角色、站点授权关联 |
| `plan` | 保留，后续向 `subscription_plan` 语义靠拢 |
| `site` | 保留，增加模板、地区、语言、币种、工作台状态等字段 |

## 7.2 新增主线表

### Site 域
- `site_template`
- `site_setting`
- `site_domain`
- `site_workspace_snapshot`

### Layout 域
- `page`
- `page_layout_version`
- `layout_block`
- `layout_publish_record`
- `layout_block_schema`
- `theme_config`

### Cloak 域
- `cloak_rule`
- `cloak_rule_version`
- `cloak_hit_log`

### Catalog / Order 域
- `product`
- `product_site_binding`
- `product_sku`
- `inventory`
- `customer`
- `order`
- `order_item`
- `shipment`

### Payment 域
- `payment_channel`
- `site_payment_binding`
- `payment_order`
- `payment_callback_log`

### ERP 域
- `erp_connector`
- `erp_sync_task`
- `erp_sync_log`

### Platform / Ops 域
- `async_task`
- `operation_log`
- `audit_log`

## 7.3 兼容保留表

以下表进入兼容期保留：
- `post`
- `post_publish`
- `task`

后续策略：
- `task` 不再新增业务用途，待统一任务中心稳定后下线
- `post`、`post_publish` 在兼容期保留，待后台不再依赖后可归档

---

## 8. 迁移脚本清单

建议按“一个主题一组脚本”的方式新增到 `docs/migrations/`。

第一批脚本建议：

| 顺序 | 脚本名 | 目的 |
|---|---|---|
| 1 | `20260410_01_expand_site_base.sql` | 扩展 `site`，补模板、语言、币种、站点状态等字段 |
| 2 | `20260410_02_create_site_template.sql` | 新建站点模板表 |
| 3 | `20260410_03_create_site_domain.sql` | 新建站点域名绑定表 |
| 4 | `20260410_04_create_site_setting.sql` | 新建站点配置表 |
| 5 | `20260410_05_create_async_task.sql` | 新建统一异步任务表 |
| 6 | `20260410_06_create_audit_log.sql` | 新建审计日志表 |
| 7 | `20260410_07_create_page_model.sql` | 新建 `page`、`page_layout_version`、`layout_block` |
| 8 | `20260410_08_create_theme_and_schema.sql` | 新建 `theme_config`、`layout_block_schema` |
| 9 | `20260410_09_create_cloak_model.sql` | 新建斗篷规则、版本和命中日志 |
| 10 | `20260410_10_create_catalog_model.sql` | 新建商品、SKU、库存表 |
| 11 | `20260410_11_create_order_model.sql` | 新建客户、订单、订单项、发货表 |
| 12 | `20260410_12_create_payment_model.sql` | 新建支付通道、绑定、支付单、回调日志 |
| 13 | `20260410_13_create_erp_model.sql` | 新建 ERP 连接器、同步任务和日志 |

执行原则：
- 所有业务表默认包含 `tenant_id`
- 外部系统标识字段需要预留 `external_id` / `provider_code`
- 金额统一使用 `DECIMAL(18,2)` 或更高精度
- JSON 配置统一明确字段语义，避免多用途大杂烩

---

## 9. 实施结论

本轮重构结论如下：
- 当前仓库可以复用认证、多租户、Multisite 建站和基础后台壳
- 需要将“内容发布”从主线降级为兼容域
- 后续开发以 `Site + Layout + Payment + Order + Cloak + ERP + Task` 为主线
- 下一步应直接进入：
  - `M11-04`：租户隔离约束核查
  - `M11-05`：统一异步任务模型设计与落库

