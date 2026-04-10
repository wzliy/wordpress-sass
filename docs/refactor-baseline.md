# 基于 docs/prod 的重构基线方案

## 1. 目标

本文档用于把当前仓库从“WordPress 接入/发布后台”迁移到 `docs/prod/*` 定义的目标系统：
- 单体站群商城
- 域名驱动前台解析
- 商品、订单、支付、发货、报表闭环

本文件同时回答 3 个问题：
- 当前仓库哪些东西还能复用
- 目标模块边界应如何调整
- 开发顺序应如何从现状迁移到 `docs/prod` 主线

产品与执行来源：
- [02-architecture.md](/Users/wangzhulin/IdeaProjects/wordpress-sass/docs/prod/02-architecture.md)
- [03-exec-plan.md](/Users/wangzhulin/IdeaProjects/wordpress-sass/docs/prod/03-exec-plan.md)

---

## 2. 当前仓库快照

### 2.1 已有可复用资产

可直接复用：
- Spring Boot 3 + Java 17 + Gradle 工程骨架
- MyBatis + H2 测试环境
- 统一响应和全局异常处理
- JWT 登录与当前用户获取
- 用户管理与基础租户上下文
- 站点创建、站点列表、基础工作台
- Vue 管理后台壳和路由
- WordPress Multisite provisioner 原型

### 2.2 与 docs/prod 的主要偏差

偏差点：
- 当前文档主线仍包含页面装修、斗篷、ERP 等重模块优先级
- 当前后端没有 `site_domain` 与 `Host` 解析能力
- 当前没有公开店铺前台控制器和模板
- 当前没有商品、订单、支付、发货、报表的完整链路
- 当前大量接口仍沿用 `/site/*`、`/post/*`、`/publish/*` 风格

### 2.3 重构结论

结论：
- 当前仓库适合继续演进，不需要推倒重建
- 但必须收缩主线，优先实现 `docs/prod` 的商城闭环
- 旧的“建站 SaaS 大平台”设想应降级为 backlog，不再占据当前迭代中心

---

## 3. 目标模块边界

## 3.1 目标领域

| 领域 | 职责 | 当前状态 | 策略 |
|---|---|---|---|
| Identity | 登录、账号、角色、权限 | 部分已有 | 复用现有 `auth/user`，逐步收敛 |
| Site | 站点基础信息、站点状态、站点设置 | 部分已有 | 继续复用 `site` |
| Domain | 域名绑定、主域名、Host 解析 | 缺失 | 新增主线领域 |
| Storefront | 前台首页/分类/商品/购物车/结账渲染 | 缺失 | 新增主线领域 |
| Catalog | 分类、商品、站点发布关系 | 缺失 | 新增主线领域 |
| Order | 购物车、订单、订单项、状态流转 | 缺失 | 新增主线领域 |
| Payment | 支付记录、提供方抽象、回调 | 缺失 | 新增主线领域 |
| Email | 邮件记录、发送抽象 | 缺失 | 新增主线领域 |
| Subsite | 子站范围的设置与订单查询 | 缺失 | 新增主线领域 |
| Shipping | 发货、物流状态、供应链查询 | 缺失 | 新增主线领域 |
| Report | 汇总报表、按站点统计 | 缺失 | 新增主线领域 |
| Legacy | WordPress 发文与兼容接口 | 已有 | 降级保留 |
| Integration | WordPress Multisite、支付、邮件外部适配器 | 部分已有 | 作为基础设施适配层保留 |

## 3.2 当前不再作为主线的领域

以下方向从“当前主线”降级为“后续 backlog”：
- 可视化页面装修引擎
- 斗篷规则引擎
- 轻 ERP / 连接器
- 复杂模板市场

原因：
- `docs/prod` 当前执行计划未把这些模块放在闭环最前面
- 这些模块实现成本高，且不解决“多域名站点可访问并可下单”这一最小可演示路径

---

## 4. 目标包结构

建议后端包结构：

```text
com.wpss.wordpresssass
├── common
├── identity
├── admin
├── site
├── domain
├── storefront
├── catalog
├── order
├── payment
├── email
├── subsite
├── shipping
├── report
├── integration
│   └── wordpress
└── legacy
    └── content
```

包结构约束：
- `admin` 负责后台聚合接口，不直接承载底层实体
- `storefront` 负责公开页面 Controller 和模板渲染
- `integration.wordpress` 只保留外部初始化适配，不承担主业务模型
- `legacy.content` 只做兼容维护，不接收新主线需求

---

## 5. 前端与模板结构

## 5.1 管理后台

管理后台继续保留 Vue，并逐步重构到以下分组：

```text
frontend/src
├── api
│   ├── admin-site.js
│   ├── admin-domain.js
│   ├── admin-catalog.js
│   ├── admin-order.js
│   ├── admin-payment.js
│   ├── admin-report.js
│   └── subsite.js
├── pages
│   ├── dashboard
│   ├── site
│   ├── domain
│   ├── catalog
│   ├── order
│   ├── payment
│   ├── report
│   ├── subsite
│   └── legacy
```

## 5.2 店铺前台

新增服务端模板目录：

```text
src/main/resources
├── templates
│   ├── storefront
│   │   ├── fallback.html
│   │   ├── home.html
│   │   ├── category.html
│   │   ├── product.html
│   │   ├── cart.html
│   │   ├── checkout.html
│   │   └── success.html
└── static
    └── storefront
```

这样做的意义：
- 管理后台仍然是 SPA
- 店铺前台则是域名驱动的 SSR/模板渲染
- 两者不混用路由职责

---

## 6. 数据迁移基线

## 6.1 可复用/扩展的表

| 表 | 策略 |
|---|---|
| `tenant` | 保留 |
| `user` | 保留并继续扩展角色权限 |
| `site` | 保留，但语义调整为平台站点基础表 |

## 6.2 新增主线表

第一批必须新增：
- `site_domain`
- `site_homepage_config`
- `category`
- `product`
- `site_product_publish`
- `orders`
- `order_item`
- `payment_record`
- `email_record`
- `shipment_record`

可选/后置：
- `cart_session`

## 6.3 脚本顺序

建议迁移顺序：
1. 扩展 `site`
2. 新增 `site_domain`
3. 新增 `site_homepage_config`
4. 新增 `category`
5. 新增 `product`
6. 新增 `site_product_publish`
7. 新增 `orders`
8. 新增 `order_item`
9. 新增 `payment_record`
10. 新增 `email_record`
11. 新增 `shipment_record`
12. 最后补 `cart_session`、种子数据和报表视图

---

## 7. 接口迁移策略

## 7.1 保留接口

当前接口可短期保留：
- `/auth/*`
- `/users/*`
- `/site/*`
- `/post/*`
- `/publish/*`

## 7.2 新主线接口

新主线统一落到：
- `/api/admin/*`
- `/api/subsite/*`
- `/api/supply/*`
- 公开店铺路由 `GET /...`

## 7.3 迁移原则

- 旧接口不立即删除，但不再承载新主线能力
- 新能力默认不追加到 `/post`、`/publish`
- 站点域名、商品、订单、支付、发货、报表全部走新路径

---

## 8. 对当前实现的现实约束

`docs/prod/03-exec-plan.md` 的 Phase 0 写的是：
- 从空仓库初始化 Maven、配置 profile、健康检查

但当前仓库现实是：
- 已经是 Gradle 项目
- 已经接入 JWT、MyBatis、H2
- 已经有 Vue 管理后台

因此本仓库的执行调整为：
- 不重做 Maven 初始化
- 保留 Gradle
- 把“Bootstrap repository”改写成“对齐仓库基础设施与交付标准”

这是规划适配，不是偏离产品方向。

---

## 9. 交付顺序结论

按照 `docs/prod`，本仓库的正确交付顺序应为：
1. Site + Domain Core
2. Homepage Config + Storefront Home
3. Category + Product Core
4. Category / Product Storefront
5. Cart + Checkout + Order
6. Payment
7. Email
8. Sub-site Basics
9. Shipping Console
10. Reports
11. Demo Hardening

在此之前，不应再把主要时间投入到：
- 斗篷
- ERP
- 可视化装修器
- 复杂模板市场

---

## 10. 文档结论

重构后的系统定位很明确：
- 后台仍是 Vue
- 业务主服务仍是 Spring Boot 单体
- 新增域名驱动的店铺前台渲染
- 主链路从“站点创建”推进到“商品展示 -> 下单 -> 支付 -> 发货 -> 报表”
- WordPress 与旧发文模块退居兼容或适配层

这就是后续任务拆分和优先级调整的基线。
