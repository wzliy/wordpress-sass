# Project Plan

## Working Rules
- 每次开始新任务前，优先检查本文件中 `status = TODO`、`status = DOING`、`status = BLOCKED` 的条目。
- `docs/prod/02-architecture.md` 和 `docs/prod/03-exec-plan.md` 是当前产品方向和任务分期的来源。
- 当旧规划与 `docs/prod/*` 冲突时，以 `docs/prod/*` 为准。
- 当前仓库不是空仓库；执行时应复用现有 Spring Boot + Gradle + Vue 基础，而不是机械照搬空仓库初始化步骤。
- 每完成一个任务，必须同步更新本文件中的状态、完成说明和日期。
- 如果某个任务被外部能力或业务定义阻塞，必须明确标记 `BLOCKED` 并写清原因。

## Status Legend
- `DONE`：已完成并已落代码或文档
- `DOING`：已开始，尚未闭环
- `TODO`：待实现
- `BLOCKED`：受外部条件或产品定义限制

## Source Alignment
- 目标系统：单机部署的模块化单体站群商城
- 主线闭环：站点 -> 域名 -> 店铺前台 -> 商品 -> 订单 -> 支付 -> 发货 -> 报表
- 保留资产：JWT、用户管理、现有站点管理、Vue 后台壳、WordPress Multisite 初始化器
- 降级模块：`post/publish` 进入兼容域，不再作为主线
- 暂缓方向：可视化装修器、斗篷、ERP 深度集成，转入后续 backlog

## Milestones

### P0 仓库对齐与基础设施
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P0-01 | 保留现有 Spring Boot 3 + Java 17 + Gradle 基线，明确不切换到 Maven | P0 | DONE | 2026-04-10 已按 `docs/prod` 重规划，确认当前仓库继续使用 Gradle |
| P0-02 | 统一基础设施基线：统一响应、异常、JWT、MyBatis、H2 | P0 | DONE | 当前仓库已有，可直接作为新主线基础 |
| P0-03 | 增加 `local/dev` profile、健康检查、启动说明 | P1 | TODO | `docs/prod` Phase 0 要求，当前仓库尚未完全对齐 |
| P0-04 | 规范化数据库迁移方式（Flyway/Liquibase 或稳定 SQL 迁移约定） | P1 | TODO | 当前仍以 SQL 文档为主，未形成正式迁移机制 |

### P1 站点与域名核心
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P1-01 | 按 `docs/prod` 重新对齐 `site` 聚合字段和后台接口语义 | P0 | DONE | 2026-04-10 已补齐 `site_code`、主题展示字段、`/api/admin/sites` 创建语义，并同步 schema / migration / API 契约 |
| P1-02 | 新增 `site_domain` 表与绑定域名 API | P0 | DONE | 2026-04-10 已新增 `site_domain` 表、自动主域名入库、`/api/admin/domains` 绑定/列表接口，并通过站点测试验证 |
| P1-03 | 实现站点详情、启用/禁用接口并收敛到 `/api/admin/sites` | P0 | DONE | 2026-04-10 已新增 `/api/admin/sites` 列表、详情、启用、停用接口，并在详情中聚合域名列表 |
| P1-04 | 实现 `Host` 域名解析服务与 fallback 页面 | P0 | DONE | 2026-04-10 已新增 Host 解析服务和公开 `/` 路由，命中站点返回占位首页，未知或停用站点返回 fallback 页 |
| P1-05 | 域名唯一性校验与主域名切换规则 | P1 | DONE | 2026-04-10 已实现全局域名唯一校验、站点创建重复域名拦截和 `primary=true` 绑定时的主域名切换 |

### P2 首页配置与店铺首页
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P2-01 | 新增 `site_homepage_config` 表并在建站时写入默认配置 | P0 | DONE | 2026-04-10 已新增 `site_homepage_config` 表、默认首页 JSON 合同仓储，并在手动接入/自动建站时自动写入 |
| P2-02 | 新增 storefront fallback 页面和首页模板 | P0 | DONE | 2026-04-10 已接入 Thymeleaf，并新增 `storefront/home`、`storefront/fallback` 模板 |
| P2-03 | 基于域名解析加载不同站点首页内容 | P0 | DONE | 2026-04-10 已通过 `Host` 解析 + `site_homepage_config` 按站点渲染首页差异内容 |
| P2-04 | 支持首页 logo、banner、featured products 的基础配置渲染 | P1 | DONE | 2026-04-10 已从首页配置渲染 logo、banner、menu 和 featured slots；真实商品卡片将在 `P3` 接入 |

### P3 分类与商品核心
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P3-01 | 新增 `category` 表与管理接口 | P0 | DONE | 2026-04-10 已新增 `category` 表、租户隔离仓储和 `/api/admin/categories` 创建/列表/启停接口 |
| P3-02 | 新增 `product` 表与管理接口 | P0 | TODO | 包括商品状态、图集、规格、价格 |
| P3-03 | 新增 `site_product_publish` 表与发布/下架接口 | P0 | TODO | 控制商品在不同站点的可见性 |
| P3-04 | 初始化 demo 分类与商品种子数据 | P1 | TODO | 供店铺前台和报表演示使用 |

### P4 分类页与商品详情页
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P4-01 | 实现分类列表与关键词搜索 | P0 | TODO | 前台公开路由 |
| P4-02 | 实现商品详情页渲染 | P0 | TODO | 展示图集、规格、价格、划线价 |
| P4-03 | 商品上下架状态联动前台可见性 | P1 | TODO | 依赖 `P3-03` |

### P5 购物车、结账与订单创建
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P5-01 | 确定购物车策略（session/cookie/持久化）并实现服务 | P0 | TODO | `cart_session` 可选 |
| P5-02 | 新增购物车页与结账表单 | P0 | TODO | 店铺前台必备 |
| P5-03 | 新增 `orders` 与 `order_item` 表并实现下单 | P0 | TODO | 最小交易闭环核心 |
| P5-04 | 成功页展示订单号和基础结果 | P1 | TODO | 对齐 `docs/prod` Phase 5 |

### P6 支付抽象
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P6-01 | 新增 `payment_record` 表与支付提供方接口 | P0 | TODO | 先定义统一抽象 |
| P6-02 | 实现 Mock 支付与回调接口 | P0 | TODO | 先跑通演示链路 |
| P6-03 | 订单支付状态联动更新 | P0 | TODO | 依赖 `P5-03` |
| P6-04 | 为 Stripe/PayPal 预留 provider stub | P1 | TODO | 先留扩展点，不抢主线 |

### P7 邮件抽象
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P7-01 | 新增 `email_record` 表与发送抽象 | P1 | TODO | `docs/prod` Phase 7 |
| P7-02 | 订单创建后触发 Mock 邮件发送 | P1 | TODO | 可先记录结果，不接真实服务 |

### P8 子站管理基础
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P8-01 | 实现 `/api/subsite/settings` 站点范围设置读取与更新 | P1 | TODO | 限制为安全字段子集 |
| P8-02 | 实现 `/api/subsite/orders` 当前站点订单查询 | P1 | TODO | 子站运营侧基础能力 |

### P9 供应链查询台
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P9-01 | 新增 `shipment_record` 表和采购/发货状态模型 | P1 | TODO | 对齐 `docs/prod` Phase 9 |
| P9-02 | 实现按订单号/物流号/邮箱查询接口 | P1 | TODO | 供应链查询入口 |
| P9-03 | 实现发货状态更新接口 | P1 | TODO | 先供 demo/admin 使用 |

### P10 汇总报表
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P10-01 | 实现总订单、已支付、已发货、收入等报表 API | P1 | TODO | 基于主链路数据聚合 |
| P10-02 | 增加按站点汇总和日期区间过滤 | P1 | TODO | 对齐 `docs/prod` Phase 10 |

### P11 Demo 收口
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P11-01 | 提供 demo seed 脚本与说明文档 | P1 | TODO | 包括默认站点、域名、分类、商品、订单、物流 |
| P11-02 | 提供本地 hosts 配置、启动路径、验证步骤 | P1 | TODO | 对齐 `docs/prod` Final hardening |
| P11-03 | 补齐端到端演示回归清单 | P1 | TODO | 覆盖建站、前台访问、下单、支付、发货、报表 |

### BX 历史兼容与延期 Backlog
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| BX-01 | 保留 `post/publish` 兼容接口，避免影响已有演示链路 | P2 | DONE | 当前可继续使用，但不再扩展 |
| BX-02 | WordPress Multisite 仅作为可选初始化器保留 | P2 | DONE | 不再作为前台运行时主路径 |
| BX-03 | 可视化页面装修器 | P3 | TODO | 从当前主线降级为后续增强 |
| BX-04 | 斗篷引擎 | P3 | TODO | 从当前主线降级为后续增强 |
| BX-05 | ERP 深度集成 | P3 | TODO | 从当前主线降级为后续增强 |

## Current Recommended Next Task
- `P3-02`：新增 `product` 表与管理接口
- 原因：分类主数据已就位，下一步需要建立商品主档，打通目录域的核心实体，为站点发布关系和 storefront 商品页提供数据来源

## Change Log
- 2026-04-08：创建计划文档，梳理旧主线完成情况
- 2026-04-09：完成旧规划中的 JWT、发布链路、工作台设计、异步任务与审计骨架等基础能力
- 2026-04-10：完成站点工作台、模板中心和自动建站初始化骨架的第一版实现
- 2026-04-10：依据 `docs/prod/02-architecture.md` 与 `docs/prod/03-exec-plan.md` 重排系统设计与任务拆分，将主线调整为“站点/域名/店铺前台/商品/订单/支付/发货/报表”
- 2026-04-10：完成 `P1-02`，新增 `site_domain` 表、自动主域名写入和 `/api/admin/domains` 绑定/列表接口
- 2026-04-10：完成 `P1-03`，新增 `/api/admin/sites` 列表、详情、启用、停用接口，并聚合站点域名
- 2026-04-10：完成 `P1-04` 与 `P1-05`，新增 Host 域名解析服务、公开 `/` fallback/占位首页，并落地全局域名唯一校验和主域名切换规则
- 2026-04-10：完成 `P1-01`，补齐 `site` 聚合的 `site_code`、主题展示字段和 `/api/admin/sites` 创建语义，并同步数据库脚本、迁移和测试断言
- 2026-04-10：完成 `P2-01`，新增 `site_homepage_config` 表和默认首页配置仓储，并在站点创建/自动建站时写入首页 JSON 合同
- 2026-04-10：完成 `P2-02`、`P2-03`、`P2-04`，引入 Thymeleaf storefront 模板，并基于 `Host` + `site_homepage_config` 渲染站点首页、banner、menu 和 featured slots
- 2026-04-10：完成 `P3-01`，新增 `catalog` 域的分类模型、`category` 表和 `/api/admin/categories` 管理接口，并补充租户隔离测试
- 2026-04-10：按 `docs/design.md` 重构前端暗色设计系统，将设计规则下沉到 `navigation` 元数据、应用壳和全局 token / 组件皮肤机制，并重做登录页、站点页和控制台视觉风格
