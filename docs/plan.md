# Project Plan

## Working Rules
- 每次开始新任务前，优先检查本文件中 `status = TODO` 或 `status = DOING` 的条目。
- 默认优先执行优先级最高、依赖最少、最靠前的未完成任务。
- 每完成一个任务，必须同步更新本文件中的状态、完成说明和日期。
- 执行过程中如果发现新的优化点、缺陷或依赖项，直接追加到对应模块任务列表。
- 除非用户明确改变方向，否则持续沿着未完成任务迭代推进。

## Status Legend
- `DONE`：已完成并已落代码或文档
- `DOING`：正在进行
- `TODO`：待实现
- `BLOCKED`：受外部条件限制

## Refactor Alignment
- 保留并复用：`M2` 认证与租户基础、`M4`/`M5` 站点接入与 Multisite 建站能力。
- 主线切换：系统主线从“文章发布到 WordPress”切换为“建站 SaaS + 页面装修 + 斗篷 + 商品订单支付 + ERP”。
- 优先级调整：文章与发布链路进入兼容维护，站点工作台、页面模型、支付、订单、斗篷与任务中心提升为主线任务。
- 实施原则：先补领域边界、数据模型和异步任务基线，再推进后台工作台和编辑器页面，避免前端原型先行导致重复返工。

## Milestones

### M1 基础架构
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M1-01 | 初始化 Spring Boot + Gradle 项目骨架 | P0 | DONE | 已完成 |
| M1-02 | 建立统一返回结构与全局异常处理 | P0 | DONE | 已完成 |
| M1-03 | 建立基础分层架构 `common / application / domain / infrastructure / interfaces` | P0 | DONE | 已完成 |
| M1-04 | 接入 MySQL + MyBatis | P0 | DONE | 已完成 |
| M1-05 | 补充测试环境 H2 兼容配置 | P0 | DONE | 已完成 |

### M2 认证与租户体系
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M2-01 | 实现管理员登录接口 `/auth/login` | P0 | DONE | 已完成 |
| M2-02 | 实现 `/auth/me` 当前用户接口 | P0 | DONE | 已完成 |
| M2-03 | 用 token 解析 `tenant_id`，替代前端手输租户 | P0 | DONE | 已完成 |
| M2-04 | 应用启动时自动初始化默认租户和管理员 | P0 | DONE | 已完成 |
| M2-05 | 增加 token 过期与无效处理完善 | P1 | DONE | 2026-04-09 已统一为 `401`，并返回过期时间元数据 |
| M2-06 | 将当前自定义 token 升级为标准 JWT | P1 | DONE | 2026-04-09 已切换为标准 JWT，包含 `iss/sub/iat/exp` |
| M2-07 | 增加登出接口与 token 失效策略 | P2 | TODO | 当前前端仅做本地清理 |

### M3 用户管理模块
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M3-01 | 实现用户列表 `/users/list` | P0 | DONE | 已完成 |
| M3-02 | 实现新增管理员 `/users/create` | P0 | DONE | 已完成 |
| M3-03 | 实现当前用户修改密码 `/users/change-password` | P0 | DONE | 已完成 |
| M3-04 | 用户表增加角色字段 `role` | P0 | DONE | 当前仅 `ADMIN` |
| M3-05 | 增加用户唯一性校验与错误提示 | P1 | DONE | 已完成 |
| M3-06 | 用户删除/禁用功能 | P1 | DONE | 2026-04-09 已实现 `ACTIVE / DISABLED`、启停接口和禁用后 token 失效 |
| M3-07 | 用户资料编辑功能 | P1 | DONE | 2026-04-09 已实现 `detail/update`、昵称字段和前端编辑页 |
| M3-08 | 多角色模型 `ADMIN / EDITOR / OPERATOR` | P2 | TODO | 当前只有管理员，按原型优先级下调 |
| M3-09 | 权限控制与接口级鉴权 | P2 | TODO | 后续配合角色系统，按原型优先级下调 |
| M3-10 | 用户手机号字段与重置密码 | P1 | TODO | 原型用户页体现出手机号与更完整账号管理 |

### M4 站点管理模块
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M4-01 | 已有 WordPress 站点接入 `/site/register` | P0 | DONE | 已完成 |
| M4-02 | 站点列表 `/site/list` | P0 | DONE | 已完成 |
| M4-03 | 站点连接测试 `/site/test` | P0 | DONE | 已完成 |
| M4-04 | 自动建站 `/site/provision` 骨架 | P0 | DONE | 已完成 |
| M4-05 | 适配 WordPress Multisite 配置与真实 provisioner | P0 | DONE | 已完成 |
| M4-06 | `status_msg` 长文本问题修复 | P0 | DONE | 已完成，SQL 已改 `TEXT` |
| M4-07 | 站点搜索、筛选、分页 | P1 | TODO | 前端已有基础能力，后续补后端化与更完整筛选 |
| M4-08 | 站点编辑能力 | P1 | TODO | 保留，但将与站点工作台一并设计，避免接口重复定义 |
| M4-09 | 站点删除/归档能力 | P1 | TODO | 需明确业务策略 |
| M4-10 | 站点健康检查批量执行 | P2 | TODO | 可配合定时任务 |
| M4-11 | 站点详情页与原型表格对齐 | P1 | TODO | 需服从新的站点工作台信息架构，不再单独作为最高优先级 |

### M5 WordPress Multisite 自动建站
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M5-01 | 明确 Multisite 建站技术路线 | P0 | DONE | 已完成 |
| M5-02 | 设计 Spring Boot -> Multisite 插件端点契约 | P0 | DONE | 已写文档 |
| M5-03 | Spring Boot Multisite HTTP 客户端 | P0 | DONE | 已完成 |
| M5-04 | Mock provisioner 兜底能力 | P0 | DONE | 已完成 |
| M5-05 | 编写 WordPress Multisite 插件原型 | P0 | DONE | 2026-04-09 已新增 `wordpress-plugin/wpss-multisite-provisioner` 原型插件 |
| M5-06 | Multisite 子站创建 + 初始化管理员 | P0 | DONE | 2026-04-09 原型插件已支持子站创建和管理员绑定 |
| M5-07 | 自动生成/返回站点应用密码 | P1 | DONE | 2026-04-09 原型插件已返回 `appPassword` |
| M5-08 | 建站失败重试与错误归档 | P1 | TODO | 目前失败处理较粗 |
| M5-09 | Multisite 网络管理监控页 | P2 | TODO | 可在 WP 插件中实现 |

### M6 内容发布兼容模块
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M6-01 | 创建文章 `/post/create` | P0 | DONE | 已完成 |
| M6-02 | 文章列表 `/post/list` | P0 | DONE | 已完成 |
| M6-03 | 草稿状态管理 | P0 | DONE | 当前默认 `DRAFT` |
| M6-04 | 文章编辑接口 | P2 | TODO | 兼容维护项，避免阻塞建站 SaaS 主线 |
| M6-05 | 文章删除/归档接口 | P2 | TODO | 兼容维护项 |
| M6-06 | 富文本编辑器集成 | P2 | TODO | 兼容维护项 |
| M6-07 | 文章标签/分类模型 | P2 | TODO | 后续接 WordPress taxonomies |

### M7 内容发布链路兼容模块
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M7-01 | 设计发布记录表与状态机 | P0 | DONE | 2026-04-09 已补 `post_publish` 设计与状态机文档 |
| M7-02 | 实现 `/publish` 接口 | P0 | DONE | 2026-04-09 已实现同步发布接口并写入发布记录 |
| M7-03 | 文章选择站点批量发布 | P0 | DONE | 2026-04-09 已新增前端发布中心页面支持按站点批量发布 |
| M7-04 | WordPress 发文客户端封装 | P0 | DONE | 2026-04-09 已扩展 `WpClient` 支持发文 |
| M7-05 | 发布失败重试机制 | P2 | DONE | 2026-04-09 已实现同步重试版，最多立即重试 3 次 |
| M7-06 | 发布结果查询页 | P2 | DONE | 2026-04-09 已实现 `/publish/list` 和前端发布历史页 |
| M7-07 | 发布异步队列/任务执行器 | P2 | DONE | 2026-04-09 已切为异步 worker，接口先返回 `PENDING` |

### M8 前端后台系统
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M8-01 | 建立 Vue 3 + Vite 前端工程 | P0 | DONE | 已完成 |
| M8-02 | 管理员登录页 | P0 | DONE | 已完成 |
| M8-03 | 标准后台布局：左侧导航、顶部二级菜单、右上角用户区 | P0 | DONE | 已完成 |
| M8-04 | 站点管理页面 | P0 | DONE | 已完成 |
| M8-05 | 文章管理页面 | P0 | DONE | 已完成 |
| M8-06 | 用户管理页面 | P0 | DONE | 已完成 |
| M8-07 | Dashboard 首页 | P1 | DONE | 2026-04-08 已完成总览首页和默认登录入口 |
| M8-08 | 列表分页、搜索、排序 | P1 | DONE | 2026-04-08 已完成站点、文章、用户列表搜索筛选分页 |
| M8-09 | 表单校验提示优化 | P1 | DONE | 2026-04-08 已补字段级校验和表单级错误提示 |
| M8-10 | 空态、加载态、错误态统一封装 | P1 | DONE | 2026-04-08 已新增通用状态组件并接入站点/文章/用户列表 |
| M8-11 | 组件拆分与复用 | P1 | DONE | 2026-04-08 已拆分布局、页面、通用组件 |
| M8-12 | 路由管理与页面级拆分 | P1 | DONE | 2026-04-08 已接入 `vue-router`，支持 URL 级页面访问 |
| M8-13 | Dashboard 原型重构 | P0 | DONE | 2026-04-09 已重构为运营看板，补齐统计卡、趋势区、终端/浏览器分布和最近动态 |
| M8-14 | 站点列表原型重构 | P1 | TODO | 应与站点工作台、建站入口、支付/斗篷状态字段一并重构 |
| M8-15 | 用户列表原型重构 | P2 | TODO | 低于建站 SaaS 主线页面 |
| M8-16 | 统一图表与统计卡组件 | P2 | TODO | 优先服务工作台和运营页，暂不单独抢占主线 |
| M8-17 | 设计 Token 与主题规范文档化 | P2 | TODO | 统一颜色、间距、表格、按钮、状态样式 |
| M8-18 | 原型对齐截图回归与验收 | P1 | TODO | 与 `docs/resource` 做逐页对照验收 |

### M9 数据库与初始化
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M9-01 | 建表脚本增加 `DROP TABLE IF EXISTS` | P0 | DONE | 已完成 |
| M9-02 | 提供管理员初始化脚本 | P0 | DONE | 已完成 |
| M9-03 | 初始化脚本改为可重复执行 | P0 | DONE | 已完成 |
| M9-04 | 数据库迁移脚本规范化 | P1 | TODO | 建议接 Flyway/Liquibase |
| M9-05 | 样例数据脚本 | P1 | TODO | 便于演示和联调 |

### M10 工程优化
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M10-01 | 将 `frontend/src/App.vue` 拆分为模块页面与组件 | P0 | DONE | 2026-04-08 已拆为布局组件、模块页面、格式化工具 |
| M10-02 | 抽离前端统一请求拦截与错误处理 | P1 | DONE | 2026-04-09 已补统一 ApiError、鉴权失效事件和网络异常映射 |
| M10-03 | 增加后端模块级单元测试和集成测试 | P1 | TODO | 当前覆盖还不够细 |
| M10-04 | 增加操作审计日志 | P2 | TODO | 用户、站点、发布等操作需追踪 |
| M10-05 | 增加配置文档与部署手册 | P2 | TODO | 目前文档还偏开发态 |
| M10-06 | 前端会话与业务状态下沉到 composable/store | P1 | DONE | 2026-04-08 已抽离 `App.vue` 会话、表单和业务状态到 composable |

### M11 重构基线与领域建模
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M11-01 | 梳理目标领域边界与包结构重组方案 | P0 | DONE | 2026-04-09 已新增 `docs/refactor-baseline.md`，明确目标领域边界、包结构和模块迁移映射 |
| M11-02 | 制定从“内容发布中心”迁移到“建站 SaaS 中心”的兼容策略 | P0 | DONE | 2026-04-09 已在 `docs/refactor-baseline.md` 明确保留、降级、禁止事项和 API 兼容策略 |
| M11-03 | 设计核心表结构与迁移脚本清单 | P0 | DONE | 2026-04-09 已在 `docs/refactor-baseline.md` 输出新增主线表清单和迁移脚本顺序 |
| M11-04 | 核查并补齐核心业务表 `tenant_id` 隔离约束 | P0 | DONE | 2026-04-09 已新增审计文档、迁移脚本，并收紧 `user/post_publish` 更新 SQL 的租户条件 |
| M11-05 | 建立统一异步任务模型 `async_task` 与任务状态机 | P0 | DONE | 2026-04-09 已新增任务设计文档、迁移脚本和 `task/domain` 基础骨架 |
| M11-06 | 扩展操作审计与关键配置变更日志模型 | P1 | DONE | 2026-04-09 已新增审计设计文档、迁移脚本和 `ops/domain` 审计模型骨架 |

### M12 站点工作台与模板中心
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M12-01 | 设计站点工作台信息架构与接口聚合模型 | P0 | DONE | 2026-04-09 已新增 `docs/site-workspace-design.md`，明确页面信息架构、聚合 DTO、主接口和模块占位规则 |
| M12-02 | 实现站点详情/工作台接口与前端页面 | P0 | TODO | 替代单纯站点列表驱动的操作流 |
| M12-03 | 建立站点模板模型与模板列表接口 | P0 | TODO | 支撑一键建站输入和模板复用 |
| M12-04 | 补齐一键建站初始化页面骨架、主题变量和默认配置 | P0 | TODO | 从仅创建子站提升到可运营站点初始化 |
| M12-05 | 站点复制能力 | P1 | TODO | 面向站群复用场景 |
| M12-06 | 站点归档、禁用与额度校验联动 | P1 | TODO | 与套餐、权限和审计配合实现 |

### M13 页面装修与布局编辑引擎
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M13-01 | 建立页面、版本、区块、主题配置数据模型 | P0 | TODO | `page / page_layout_version / layout_block / theme_config` |
| M13-02 | 设计区块 Schema 注册机制 | P0 | TODO | 让后台通过配置驱动区块属性，而不是硬编码页面 |
| M13-03 | 实现页面树、草稿保存、发布、回滚接口 | P0 | TODO | 覆盖首页、落地页、结账页、成功页等类型 |
| M13-04 | 实现可视化编辑器 MVP 前端 | P0 | TODO | 包含页面树、区块库、画布、属性面板、预览/发布 |
| M13-05 | 全局页头/页脚与主题变量编辑 | P1 | TODO | 作为编辑器第一轮增强项 |
| M13-06 | 模板保存、模板复用、多语言字段扩展 | P1 | TODO | 放在 MVP 之后推进 |

### M14 斗篷引擎
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M14-01 | 设计斗篷规则、规则版本、命中日志模型 | P0 | TODO | `cloak_rule / cloak_rule_version / cloak_hit_log` |
| M14-02 | 实现规则条件 DSL 与执行链路 | P0 | TODO | 支持国家、IP、ASN、UA、Referer、UTM、白黑名单等条件 |
| M14-03 | 实现斗篷管理后台 MVP | P1 | TODO | 规则列表、排序、开关、灰度、日志查看 |
| M14-04 | 建立低延迟判定接口与异步日志写入 | P1 | TODO | 保证命中日志不阻塞主请求 |
| M14-05 | 增加规则变更审计与手动停用能力 | P1 | TODO | 高风险配置必须可追溯 |

### M15 商品、订单、支付与 ERP 中台
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M15-01 | 设计商品、SKU、库存、客户、订单核心模型 | P0 | TODO | `product / product_sku / inventory / customer / order / order_item` |
| M15-02 | 实现商品中心基础版接口与列表页 | P1 | TODO | 支撑站点选品与基础维护 |
| M15-03 | 实现订单中心基础版接口与列表页 | P1 | TODO | 提供订单状态、支付状态、发货状态查询 |
| M15-04 | 建立统一支付通道接口与支付单状态机 | P0 | TODO | `payment_channel / payment_order / payment_callback_log` |
| M15-05 | 实现站点支付绑定、回调验签和补偿处理 | P0 | TODO | 支付是建站上线闭环的关键路径 |
| M15-06 | 设计 ERP 连接器、同步任务和日志模型 | P1 | TODO | 先支持轻 ERP + 连接器模式 |
| M15-07 | 实现订单/商品 ERP 同步 MVP | P1 | TODO | 至少打通一种连接器链路 |

### M16 域名、SSL 与运营支撑
| ID | Task | Priority | Status | Notes |
|---|---|---:|---|---|
| M16-01 | 建立域名绑定、DNS 检测、证书状态模型 | P1 | TODO | 对齐域名与 SSL 中心设计 |
| M16-02 | 实现 DNS/SSL 适配器与任务调度串联 | P1 | TODO | 统一走异步任务中心 |
| M16-03 | 建立站点、订单、支付、斗篷的工作台统计口径 | P1 | TODO | 为 Dashboard 和站点工作台提供统一指标 |
| M16-04 | 异常告警与失败任务重试策略 | P1 | TODO | 覆盖建站、支付、ERP、证书等任务 |
| M16-05 | 数据看板增强 | P2 | TODO | 在主链路稳定后补齐趋势、成功率和来源分析 |

## Current Recommended Next Task
- `M12-02`：实现站点详情/工作台接口与前端页面
- 原因：`M12-01` 已明确工作台页面结构、聚合 DTO、主接口和模块占位规则，下一步可以按设计直接落地后端聚合接口与前端工作台页，替代当前站点列表驱动的操作流

## Change Log
- 2026-04-08：创建计划文档，梳理已完成与待办任务
- 2026-04-08：完成 `M10-01` 和 `M8-11`，前端后台拆分为布局组件、模块页面和通用格式化工具
- 2026-04-08：完成 `M8-12`，前端接入 `vue-router` 并切换为 URL 驱动页面导航
- 2026-04-08：完成 `M8-10`，新增统一状态组件处理加载、空态和错误展示
- 2026-04-08：完成 `M8-09`，为登录、站点、文章、用户相关表单增加字段级校验和表单错误提示
- 2026-04-08：完成 `M8-07`，新增 Dashboard 首页并调整为后台默认入口
- 2026-04-08：完成 `M8-08`，为站点、文章、用户列表增加搜索、筛选、排序与分页
- 2026-04-08：完成 `M10-06`，将前端会话、表单和业务状态从 `App.vue` 下沉到 composable
- 2026-04-09：完成 `M10-02`，统一前端请求错误模型、鉴权失效处理和网络异常映射
- 2026-04-09：完成 `M7-01` 到 `M7-04`，补齐发布状态机、发布接口、WordPress 发文客户端和前端发布中心
- 2026-04-09：完成 `M7-05`，实现同步重试版发布机制并记录重试次数
- 2026-04-09：完成 `M7-06`，新增发布记录列表接口和前端发布历史页
- 2026-04-09：完成 `M7-07`，引入异步发布执行器，发布接口改为先返回 `PENDING`
- 2026-04-09：完成 `M5-05` 到 `M5-07`，新增 WordPress Multisite 插件原型，支持子站创建、管理员绑定和应用密码返回
- 2026-04-09：完成 `M2-05`，认证异常统一返回 `401`，登录响应增加 token 过期时间元数据
- 2026-04-09：完成 `M2-06`，认证令牌切换为标准 JWT，实现 `iss/sub/iat/exp` 和签名校验
- 2026-04-09：完成 `M3-06`，为用户增加 `ACTIVE / DISABLED` 状态、启停接口、前端操作入口和禁用后鉴权失效
- 2026-04-09：完成 `M3-07`，为用户增加昵称字段、详情与资料更新接口，并接入前端编辑页
- 2026-04-09：新增 `docs/ui-design-proposal.md`，并基于原型重构任务优先级，新增 `M8-13` 到 `M8-18`
- 2026-04-09：完成 `M8-13`，Dashboard 重构为更接近原型的运营总览页
- 2026-04-09：依据 `docs/architecture.md` 和 `docs/requirement-breakdown.md` 重排任务优先级，下调内容发布主线，新增 `M11` 到 `M16` 覆盖建站 SaaS 重构任务
- 2026-04-09：完成 `M11-01` 到 `M11-03`，新增 `docs/refactor-baseline.md`，明确领域边界、兼容迁移策略、核心表结构与脚本清单
- 2026-04-09：完成 `M11-04`，新增 `docs/tenant-isolation-audit.md` 和 `docs/migrations/20260409_harden_tenant_isolation.sql`，并将关键更新 SQL 收紧为 `id + tenant_id`
- 2026-04-09：完成 `M11-05`，新增 `docs/async-task-design.md`、`docs/migrations/20260409_create_async_task.sql` 和 `task/domain` 任务模型骨架
- 2026-04-09：完成 `M11-06`，新增 `docs/audit-log-design.md`、`docs/migrations/20260409_create_audit_log.sql` 和 `ops/domain` 审计模型骨架
- 2026-04-09：完成 `M12-01`，新增 `docs/site-workspace-design.md`，明确站点工作台信息架构、接口聚合模型和模块占位策略
