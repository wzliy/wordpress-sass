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
| P0-03 | 增加 `local/dev` profile、健康检查、启动说明 | P1 | DONE | 2026-04-11 已新增 `application-local.yml` / `application-dev.yml`、Actuator health 端点和 README 启动说明 |
| P0-04 | 规范化数据库迁移方式（Flyway/Liquibase 或稳定 SQL 迁移约定） | P1 | DONE | 2026-04-11 已补 `docs/migration-guidelines.md`，正式收口 `docs/migrations/*.sql` 的命名和执行约定 |

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
| P3-02 | 新增 `product` 表与管理接口 | P0 | DONE | 2026-04-11 已新增 `product` 表、商品主档仓储和 `/api/admin/products` 创建/更新/列表/启停接口 |
| P3-03 | 新增 `site_product_publish` 表与发布/下架接口 | P0 | DONE | 2026-04-11 已新增 `site_product_publish` 表、商品按站点/全站发布矩阵接口，并让 storefront 首页按发布状态过滤 featured 商品 |
| P3-04 | 初始化 demo 分类与商品种子数据 | P1 | DONE | 2026-04-11 已新增可开关的 demo catalog bootstrap，为默认租户初始化 3 个分类和 6 个 ACTIVE 商品，并在测试环境默认关闭 |

### P4 分类页与商品详情页
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P4-01 | 实现分类列表与关键词搜索 | P0 | DONE | 2026-04-11 已新增 `GET /category/{slug}` 公开路由，按 Host + 分类 slug + 关键词 q 查询当前站点已发布商品 |
| P4-02 | 实现商品详情页渲染 | P0 | DONE | 2026-04-11 已新增 `GET /product/{id}` 公开路由和 `storefront/product` 模板，渲染图集、规格、价格、划线价与描述内容 |
| P4-03 | 商品上下架状态联动前台可见性 | P1 | DONE | 2026-04-11 已将 storefront 首页 featured、分类页和详情页统一收口为“`ACTIVE` + 已发布”可见 |

### P5 购物车、结账与订单创建
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P5-01 | 确定购物车策略（session/cookie/持久化）并实现服务 | P0 | DONE | 2026-04-11 已确定 demo 使用按 `siteId` 分桶的 server-side `HttpSession` 购物车服务，不落 `cart_session` 表 |
| P5-02 | 新增购物车页与结账表单 | P0 | DONE | 2026-04-11 已新增 `GET /cart`、`POST /cart/items*`、`GET /checkout` 及对应 storefront 模板 |
| P5-03 | 新增 `orders` 与 `order_item` 表并实现下单 | P0 | DONE | 2026-04-11 已新增 `orders`、`order_item` 表与 `POST /checkout` 下单逻辑，按 demo 运费规则写入订单与订单项 |
| P5-04 | 成功页展示订单号和基础结果 | P1 | DONE | 2026-04-11 已新增 `GET /order/{orderNo}/success` 成功页，展示订单号、金额摘要和基础状态 |

### P6 支付抽象
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P6-01 | 新增 `payment_record` 表与支付提供方接口 | P0 | DONE | 2026-04-11 已新增 `payment_record` 表、`PaymentProvider` 抽象和租户隔离仓储，并同步 schema / migration |
| P6-02 | 实现 Mock 支付与回调接口 | P0 | DONE | 2026-04-11 已新增 `/payments/orders/{orderNo}/initiate`、`/payments/mock/{paymentNo}`、`/payments/mock/{paymentNo}/callback` 和 mock 支付页 |
| P6-03 | 订单支付状态联动更新 | P0 | DONE | 2026-04-11 Mock 回调成功后会将 `orders.payment_status` 与 `orders.order_status` 联动更新为 `PAID` |
| P6-04 | 为 Stripe/PayPal 预留 provider stub | P1 | DONE | 2026-04-11 已落 `PAYPAL` / `STRIPE` provider stub，保留后续真实接入扩展点 |

### P7 邮件抽象
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P7-01 | 新增 `email_record` 表与发送抽象 | P1 | DONE | 2026-04-11 已新增 `email_record` 表、`EmailSender` 抽象、Mock sender 和租户隔离仓储，并同步 schema / migration |
| P7-02 | 订单创建后触发 Mock 邮件发送 | P1 | DONE | 2026-04-11 checkout 成功后会自动写入 `ORDER_PLACED` 邮件记录，并将 mock 发送结果标记为 `SENT` |

### P8 子站管理基础
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P8-01 | 实现 `/api/subsite/settings` 站点范围设置读取与更新 | P1 | DONE | 2026-04-11 已新增 `/api/subsite/settings` 读取/更新接口，收口安全字段子集，并让手动建站也初始化 `site_setting` / `theme_config` |
| P8-02 | 实现 `/api/subsite/orders` 当前站点订单查询 | P1 | DONE | 2026-04-11 已新增 `/api/subsite/orders`，支持当前站点订单按订单号、订单状态、支付状态和日期区间筛选 |

### P9 供应链查询台
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P9-01 | 新增 `shipment_record` 表和采购/发货状态模型 | P1 | DONE | 2026-04-11 已新增 `shipment_record` 表、采购/发货状态模型，并将订单默认 `shipping_status` 收口为 `NOT_SHIPPED` |
| P9-02 | 实现按订单号/物流号/邮箱查询接口 | P1 | DONE | 2026-04-11 已新增 `/api/supply/shipments`，支持按订单号、物流号、客户邮箱查询供应链状态 |
| P9-03 | 实现发货状态更新接口 | P1 | DONE | 2026-04-11 已新增 `/api/supply/shipments/{orderNo}` 更新接口，并同步回写 `orders.shipping_status` |

### P10 汇总报表
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P10-01 | 实现总订单、已支付、已发货、收入等报表 API | P1 | DONE | 2026-04-11 已新增 `/api/admin/reports`，返回总订单、已支付、已发货和收入汇总 |
| P10-02 | 增加按站点汇总和日期区间过滤 | P1 | DONE | 2026-04-11 报表接口已支持 `siteId`、`dateFrom`、`dateTo` 过滤，并返回 `siteSummaries` |

### P11 Demo 收口
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| P11-01 | 提供 demo seed 脚本与说明文档 | P1 | DONE | 2026-04-11 已新增 `scripts/demo-seed.sh`，通过现有 API 创建/复用 demo 站点、分类、商品，并生成 paid / pending 订单与物流演示数据 |
| P11-02 | 提供本地 hosts 配置、启动路径、验证步骤 | P1 | DONE | 2026-04-11 已新增 `docs/demo-runbook.md`，收口 hosts 配置、启动命令、seed 步骤和手动验证路径 |
| P11-03 | 补齐端到端演示回归清单 | P1 | DONE | 2026-04-11 已新增 `docs/demo-regression-checklist.md`，覆盖健康检查、前台访问、下单、支付、发货、子站运营和报表回归 |

### BX 历史兼容与延期 Backlog
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| BX-01 | 保留 `post/publish` 兼容接口，避免影响已有演示链路 | P2 | DONE | 当前可继续使用，但不再扩展 |
| BX-02 | WordPress Multisite 仅作为可选初始化器保留 | P2 | DONE | 不再作为前台运行时主路径 |
| BX-03 | 可视化页面装修器 | P3 | DONE | 2026-04-11 已完成首页到系统页的草稿、预览、发布、版本回滚闭环 |
| BX-03-01 | 定义页面装修器领域模型、发布机制与任务拆分 | P3 | DONE | 2026-04-11 已新增 `docs/page-editor-design.md`，明确 `page` 模块、`page/page_layout_version` 表和“发布编译到 `site_homepage_config`”的兼容机制 |
| BX-03-02 | 新增 `page`、`page_layout_version` 表与仓储骨架 | P3 | DONE | 2026-04-11 已新增 `page` 模块、两张页面表及仓储骨架，并在站点接入/建站时自动初始化 `HOME` 页面和初始已发布版本 |
| BX-03-03 | 实现编辑器查询接口和首页草稿保存接口 | P3 | DONE | 2026-04-11 已新增 `/api/admin/sites/{siteId}/pages*` 页面列表、编辑器载荷和首页草稿保存接口，草稿保存会自动从已发布版本分叉 `DRAFT` 版本 |
| BX-03-04 | 实现首页预览/发布与工作台入口联动 | P3 | DONE | 2026-04-11 已新增首页预览/发布接口、`PageRuntimeCompiler` 和工作台到 `/sites/{id}/pages/home/editor` 的真实入口，并补了 Vue 首页编辑器壳 |
| BX-03-05 | 增加版本历史与更多页面类型 | P3 | DONE | 2026-04-11 已扩展 `PRODUCT / CHECKOUT / SUCCESS` 页面初始化与编辑器，并新增版本列表、回滚接口和前端历史面板 |
| BX-04 | 斗篷引擎 | P3 | DOING | 2026-04-11 已完成设计基线，后续按 BX-04-01 到 BX-04-05 分阶段落地 |
| BX-04-01 | 定义斗篷规则模型、仿真机制与任务拆分 | P3 | DONE | 2026-04-11 已新增 `docs/cloak-engine-design.md`，明确 `cloak_rule / cloak_hit_log`、仿真优先和非目标边界 |
| BX-04-02 | 新增 `cloak_rule`、`cloak_hit_log` 表与仓储骨架 | P3 | DONE | 2026-04-11 已新增两张斗篷表、MyBatis 仓储、迁移脚本和仓储测试，仍未接真实 storefront 请求链路 |
| BX-04-03 | 实现规则创建、编辑、排序、启停接口 | P3 | TODO | 站点范围管理，启停变更需接审计 |
| BX-04-04 | 实现命中仿真与日志查询接口 | P3 | TODO | 引入 `CloakEvaluationService`，先做后台仿真，不做真实流量斗篷 |
| BX-04-05 | 接通工作台入口和后台管理页 | P3 | TODO | 将 `GO_CLOAK` 接到真实路由和规则列表页 |
| BX-05 | ERP 深度集成 | P3 | TODO | 从当前主线降级为后续增强 |

## Current Recommended Next Task
- `BX-04-03`：实现规则创建、编辑、排序、启停接口
- 原因：斗篷规则和命中日志表已经落库，下一步应补站点范围管理接口，把工作台和后台页能真正连到可操作资源

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
- 2026-04-11：完成 `P3-02`，新增 `product` 表、商品主档模型和 `/api/admin/products` 创建/更新/列表/启停接口，并补充商品目录域测试
- 2026-04-11：完成 `P3-03`，新增 `site_product_publish` 表、`/api/admin/products/{productId}/publishes` 发布矩阵与按站点/全站发布下架接口，并让 storefront 首页 featured 商品受发布状态控制
- 2026-04-11：完成 `P3-04`，新增可开关的 demo catalog bootstrap，为默认租户初始化 3 个分类与 6 个商品，并补充独立种子测试
- 2026-04-11：完成 `P4-01`，新增 `GET /category/{slug}` 分类页模板和按 Host + 分类 slug + 关键词 `q` 查询已发布商品的 storefront 公开读模型
- 2026-04-11：完成 `P4-02`，新增 `GET /product/{id}` 商品详情页模板，支持图集、规格、价格、划线价和描述渲染
- 2026-04-11：完成 `P4-03`，将 storefront 首页 featured、分类页和详情页统一改为仅展示 `ACTIVE` 且已发布到当前站点的商品
- 2026-04-11：完成 `P5-01` 与 `P5-02`，确定 demo 购物车使用按站点分桶的 session-based 策略，并新增 `GET /cart`、`POST /cart/items*` 和 `GET /checkout` 模板链路
- 2026-04-11：完成 `P5-03` 与 `P5-04`，新增 `orders`、`order_item` 表、`POST /checkout` 下单逻辑和 `GET /order/{orderNo}/success` 成功页，跑通最小下单闭环
- 2026-04-11：完成 `P6-01` 到 `P6-04`，新增 `payment_record`、支付 provider 抽象、Mock 支付页与回调链路，并在成功回调后将订单状态联动更新为 `PAID`
- 2026-04-11：完成 `P7-01` 与 `P7-02`，新增 `email_record`、邮件 sender 抽象和 mock sender，并在 checkout 成功后自动写入订单通知记录
- 2026-04-11：完成 `P8-01` 与 `P8-02`，新增 `/api/subsite/settings` 与 `/api/subsite/orders`，补齐子站运营侧最小设置和订单查询能力
- 2026-04-11：完成 `P9-01` 到 `P9-03`，新增 `shipment_record`、供应链查询接口和发货状态更新接口，并将物流状态回写到订单
- 2026-04-11：完成 `P10-01` 与 `P10-02`，新增 `/api/admin/reports` 汇总报表接口，支持按站点和日期区间聚合订单、支付、发货与收入数据
- 2026-04-11：完成 `P0-03` 与 `P0-04`，新增 `local/dev` profile、健康检查、启动说明，并文档化 `docs/migrations/*.sql` 的稳定迁移约定
- 2026-04-11：完成 `P11-01` 到 `P11-03`，新增 `scripts/demo-seed.sh`、`docs/demo-runbook.md` 和 `docs/demo-regression-checklist.md`，收口本地 demo 数据、hosts/启动说明和端到端演示回归路径
- 2026-04-11：启动 `BX-03`，新增 `docs/page-editor-design.md` 并在架构文档中补充 `page` 模块和页面发布编译机制，将可视化页面装修器拆分为可执行子任务
- 2026-04-11：完成 `BX-03-02`，新增 `page`、`page_layout_version` 表、MyBatis 仓储和 `PageBootstrapService`，并让站点创建流程自动生成默认首页页面与初始版本
- 2026-04-11：完成 `BX-03-03`，新增页面列表、编辑器载荷和首页草稿保存接口，并支持从已发布首页版本自动分叉 `DRAFT` 版本
- 2026-04-11：完成 `BX-03-04`，新增首页预览/发布接口、`PageRuntimeCompiler`、工作台真实编辑入口和 Vue 首页编辑器壳，打通“草稿 -> 预览 -> 发布”闭环
- 2026-04-11：完成 `BX-03-05`，新增页面版本列表与回滚接口，将系统页面扩展到 `HOME / PRODUCT / CHECKOUT / SUCCESS`，并把前端编辑器升级为多页面切换和历史版本面板
- 2026-04-11：启动 `BX-04`，新增 `docs/cloak-engine-design.md`，明确斗篷规则、命中仿真、命中日志和工作台入口的最小实现边界，并将后续任务拆分为 `BX-04-01` 到 `BX-04-05`
- 2026-04-11：完成 `BX-04-02`，新增 `cloak_rule`、`cloak_hit_log` 表、MyBatis 仓储、迁移脚本和仓储测试，为后续斗篷规则管理接口提供 schema 基线
