# 站群商城系统架构文档

## 1. 文档目标

本文档用于基于 `docs/prod/02-architecture.md` 和 `docs/prod/03-exec-plan.md`，重新定义当前仓库的目标架构。

本轮重规划后的原则：
- `docs/prod/*` 作为产品方向和交付节奏的上位输入
- 当前仓库的实现现实也必须被尊重：Spring Boot 3、Java 17、Gradle、Vue 管理后台已存在
- 因此本架构不是“从零搭新仓库”，而是“在现有仓库上向 `docs/prod` 目标迁移”

当旧文档与 `docs/prod/*` 冲突时，以 `docs/prod/*` 为准。

---

## 2. 产品定位

目标系统不再以“WordPress 内容发布后台”为中心，而是以“站群商城后台 + 店铺前台 + 供应链查询”作为主线。

核心能力：
- 平台管理员创建和管理多个站点
- 不同域名命中不同站点，并由平台直接渲染店铺前台
- 商家维护商品、发布到站点、接单、收款、发货、查看报表
- 子站运营人员只管理当前站点的有限配置和订单

当前阶段的重点不是：
- 可视化页面装修引擎优先
- 斗篷引擎优先
- ERP 深度集成优先

这些能力可以保留为后续扩展，但不再作为当前主线交付顺序。

---

## 3. 目标架构

目标形态：单机部署、模块化单体。

```text
[Browser]
  -> [Nginx]
       -> admin static assets (Vue)
       -> Spring Boot App
            -> admin APIs
            -> storefront controllers
            -> sub-site APIs
            -> supply-chain APIs
            -> MySQL
```

补充说明：
- 管理后台继续使用现有 Vue 前端
- 店铺前台由 Spring Boot 直接承接域名解析与 HTML 渲染
- WordPress Multisite 不再是前台运行时中心，只保留为可选的站点初始化/兼容适配能力
- 所有核心业务数据统一落 MySQL

---

## 4. 关键运行流程

### 4.1 管理员创建站点

流程：
1. 管理后台调用站点创建接口
2. 后端写入 `site`
3. 后端写入 `site_domain`
4. 后端写入默认首页配置 `site_homepage_config`
5. 站点状态从 `INIT` 进入 `ACTIVE`

如果启用了 WordPress Multisite 适配器：
- 可在站点创建后异步触发底层子站初始化
- 但这不改变平台前台域名解析和店铺渲染仍由 Spring Boot 承担

### 4.2 顾客访问域名

流程：
1. Nginx 保留原始 `Host`
2. Spring Boot 根据 `Host` 查询 `site_domain`
3. 命中站点后加载站点基础信息与首页配置
4. 渲染对应站点的首页内容
5. 如果未命中域名，则返回 fallback 页面

### 4.3 顾客下单

流程：
1. 顾客浏览商品并进入结账
2. 系统创建订单与订单项
3. 系统创建支付记录
4. Mock 支付或真实支付回调更新支付状态
5. 订单进入报表和供应链查询视图

### 4.4 子站或供应链查询

流程：
1. 子站运营端只读取站点自身可编辑配置和订单
2. 供应链端按订单号、邮箱、物流号查询订单履约状态
3. 发货状态更新后回写订单视图和报表口径

---

## 5. 模块边界

## 5.1 后端模块

建议模块如下：

| 模块 | 职责 |
|---|---|
| `common` | 枚举、异常、统一响应、工具、租户上下文 |
| `identity` | 登录、账号、角色、权限、租户归属 |
| `admin` | 平台后台聚合接口、管理侧应用服务 |
| `site` | 站点基础信息、站点设置、启停、站点状态 |
| `domain` | 域名绑定、主域名、域名状态、Host 解析 |
| `storefront` | 前台首页、分类页、商品页、购物车、结账、成功页 |
| `catalog` | 分类、商品、站点发布关系 |
| `order` | 购物车快照、订单、订单项、订单状态 |
| `payment` | 支付记录、支付提供方抽象、回调处理 |
| `email` | 邮件发送抽象、邮件记录、事件触发 |
| `subsite` | 子站管理接口，只暴露站点范围内能力 |
| `shipping` | 采购/发货/物流记录与查询 |
| `report` | 平台汇总报表、按站点统计 |
| `legacy` | 当前仓库遗留的 WordPress 发文与兼容逻辑 |
| `integration.wordpress` | Multisite 初始化等可选适配器 |

## 5.2 当前仓库的落地约束

当前仓库不是空仓库，因此采用以下约束：
- 保留 `auth`、`user`、`site` 现有能力，逐步迁入目标边界
- `post`、`publish` 保留为兼容域，不再扩张
- `wordpress-plugin/wpss-multisite-provisioner` 保留为可选适配能力
- 新主线优先补 `domain / storefront / catalog / order / payment / shipping / report`

---

## 6. 分层设计

统一采用四层：
- `interfaces`：Controller、请求/响应 DTO、页面聚合接口
- `application`：业务编排、事务边界、跨模块协作
- `domain`：实体、值对象、仓储接口、状态机、领域规则
- `infrastructure`：MyBatis、模板渲染、外部适配器、事件发送

特殊说明：
- `storefront` 会同时包含公开访问的 Controller 和模板渲染逻辑
- `admin` 更偏 BFF / 聚合层，不承载底层实体持久化
- `domain` 模块不直接依赖 Vue 前端或 WordPress 插件实现

---

## 7. 前后端架构分工

## 7.1 管理后台

管理后台继续使用 Vue：
- 站点管理
- 域名管理
- 商品管理
- 订单管理
- 支付与报表
- 子站运营视图

## 7.2 店铺前台

店铺前台由 Spring Boot 直接渲染：
- 首页 `GET /`
- 分类页 `GET /category/{slug}`
- 商品详情 `GET /product/{id}`
- 购物车 `GET /cart`
- 加购 `POST /cart/items`
- 结账 `POST /checkout`
- 成功页 `GET /order/{orderNo}/success`

这样做的原因：
- `docs/prod` 明确要求按域名直达店铺
- 当前阶段先保证“多域名、多站点、下单闭环”跑通
- 前台不依赖另起一个 SPA 或独立前端仓库

---

## 8. 数据模型基线

除 `tenant`、`user` 等复用表外，主线新增或重定义以下核心表：

### 8.1 站点域
- `site`
- `site_domain`
- `site_homepage_config`

### 8.2 商品域
- `category`
- `product`
- `site_product_publish`

### 8.3 订单域
- `cart_session`（可选）
- `orders`
- `order_item`

### 8.4 支付与通知
- `payment_record`
- `email_record`

### 8.5 供应链与报表
- `shipment_record`

### 8.6 仓库适配规则

由于当前仓库已有多租户基础，因此新增业务表默认补 `tenant_id`，即使 `docs/prod` 示例表未显式列出，也应在实际实现中加上。

关键约束：
- `site_domain.domain` 必须全局唯一
- 站点启停应同步影响域名解析与前台可访问性
- 订单、支付、发货记录必须可追溯
- 店铺首页配置与站点基础信息分离，避免把前台结构塞进 `site` 大表

---

## 9. API 分组

## 9.1 Admin APIs

建议统一到：
- `/api/admin/sites`
- `/api/admin/domains`
- `/api/admin/categories`
- `/api/admin/products`
- `/api/admin/orders`
- `/api/admin/payments`
- `/api/admin/reports`

当前仓库已有 `/site/*`、`/users/*` 等接口，可作为兼容路径保留，但新主线优先落到 `/api/admin/*`。

## 9.2 Storefront Routes

公开路由：
- `GET /`
- `GET /category/{slug}`
- `GET /product/{id}`
- `GET /cart`
- `POST /cart/items`
- `POST /checkout`
- `GET /order/{orderNo}/success`

## 9.3 Sub-site APIs

建议：
- `/api/subsite/settings`
- `/api/subsite/orders`

## 9.4 Supply-chain APIs

建议：
- `/api/supply/orders`
- `/api/supply/shipments`

---

## 10. 当前仓库的迁移结论

基于 `docs/prod`，当前仓库的主线迁移结论如下：
- `auth/user/site` 继续复用，但目标不再是 WordPress 发布后台
- `site` 从“接入 WordPress 站点”转为“平台站点 + 域名 + 首页配置 + 前台路由入口”
- `post/publish` 降级为兼容模块
- `WordPress Multisite` 降级为可选初始化器，而不是系统前台的核心运行方式
- 当前最高优先级从“页面装修/斗篷/ERP”切换为“域名解析 + 店铺前台 + 商品 + 订单 + 支付 + 发货 + 报表”

---

## 11. 非目标

当前阶段不作为主线目标：
- 完整可视化页面装修器
- 斗篷规则引擎
- 深度 ERP 双向同步
- 微服务拆分
- 多仓库前后端彻底拆分

这些能力保留为后续 backlog，但不应阻塞 `docs/prod` 主线交付。
