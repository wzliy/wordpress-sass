# API Design

## Auth

POST /auth/login
→ return JWT token

JWT payload MUST include:
- user_id
- tenant_id
- `iss`
- `sub`
- `iat`
- `exp`

Response fields:
- `token`
- `userId`
- `tenantId`
- `username`
- `email`
- `nickname`
- `role`
- `expiresAt`
- `expireSeconds`

GET /auth/me
→ return current user info

Rules:
- invalid token -> `401`
- expired token -> `401`
- missing `Authorization` -> `401`

---

## User APIs

### List Users
GET /users/list

### Create User
POST /users/create

Request:
{
"username": "editor01",
"password": "admin123",
"email": "editor01@example.com"
}

### Change Password
POST /users/change-password

Request:
{
"currentPassword": "admin123",
"newPassword": "newpass123"
}

### Disable User
POST /users/disable

Request:
{
"userId": 2
}

### Enable User
POST /users/enable

Request:
{
"userId": 2
}

### User Detail
GET /users/detail?id=2

### Update User Profile
POST /users/update

Request:
{
"userId": 2,
"email": "editor01@example.com",
"nickname": "内容运营"
}

---

## Site APIs

### Add Site
POST /site/add

Alias:
POST /site/register

Admin resource alias:
POST /api/admin/sites

Request:
{
"name": "My Blog",
"baseUrl": "https://example.com",
"wpUsername": "admin",
"appPassword": "xxxx"
}

Response site fields include:
- `siteCode`
- `themeColor`
- `logoUrl`
- `bannerTitle`
- `bannerSubtitle`

### Provision Site
POST /site/provision

Request:
{
"name": "Tenant Blog",
"adminEmail": "owner@example.com",
"templateCode": "starter-one-product",
"countryCode": "US",
"languageCode": "en",
"currencyCode": "USD",
"subdomainPrefix": "tenant-blog"
}

---

### Admin Site List
GET /api/admin/sites

### Admin Site Detail
GET /api/admin/sites/{id}

Response:
```json
{
  "success": true,
  "data": {
    "site": {
      "id": 1,
      "tenantId": 1001,
      "siteCode": "main-site-a1b2c3d4",
      "name": "Main Site",
      "siteType": "REGISTERED",
      "baseUrl": "https://main.example",
      "domain": "https://main.example",
      "adminUrl": "https://main.example/wp-admin",
      "authType": "APP_PASSWORD",
      "wpUsername": "admin",
      "status": 1,
      "provisionStatus": "NONE",
      "statusMessage": "CREATED",
      "templateId": null,
      "countryCode": null,
      "languageCode": null,
      "currencyCode": null,
      "themeColor": "#2563EB",
      "logoUrl": null,
      "bannerTitle": "Main Site",
      "bannerSubtitle": "Your storefront is ready to be customized.",
      "createdAt": "2026-04-10T17:20:00"
    },
    "domains": [
      {
        "id": 1,
        "siteId": 1,
        "domain": "main.example",
        "primary": true,
        "status": "ACTIVE",
        "expiryAt": null,
        "createdAt": "2026-04-10T17:20:00"
      }
    ]
  },
  "message": "OK"
}
```

### Disable Site
POST /api/admin/sites/{id}/disable

### Enable Site
POST /api/admin/sites/{id}/enable

Rules:
- 仅允许操作当前租户下的站点
- 启停会更新站点状态与状态说明，后续 Host 解析只会放行启用站点

### List Site Pages
GET /api/admin/sites/{siteId}/pages

Response fields:
- `pageId`
- `siteId`
- `pageKey`
- `pageName`
- `pageType`
- `status`
- `currentVersionId`
- `publishedVersionId`
- `updatedAt`

Rules:
- 当前会自动初始化 `HOME / PRODUCT / CHECKOUT / SUCCESS` 四个系统页面
- 仅返回当前租户、当前站点下的页面

### Get Page Editor Payload
GET /api/admin/sites/{siteId}/pages/{pageKey}/editor

Response fields:
- `pageKey`
- `pageName`
- `pageType`
- `pageStatus`
- `currentVersionId`
- `publishedVersionId`
- `currentVersionNo`
- `currentVersionStatus`
- `layout`
- `blockLibrary`

Rules:
- `layout` 返回当前编辑中的版本；如果没有独立草稿，则返回当前已发布版本
- `blockLibrary` 会根据 `pageKey` 返回页面类型感知的静态区块定义

### Save Page Draft
PUT /api/admin/sites/{siteId}/pages/{pageKey}/draft

Request:
```json
{
  "layout": {
    "pageKey": "HOME",
    "sections": [
      {
        "id": "hero-1",
        "type": "hero-banner",
        "props": {
          "title": "Updated banner"
        }
      }
    ]
  },
  "versionNote": "First draft update"
}
```

Rules:
- `layout.pageKey` 必须与路径中的 `{pageKey}` 一致
- `layout.sections` 必须是数组
- 当当前版本仍是已发布版本时，首次保存草稿会自动分叉出新的 `DRAFT` 版本
- 草稿保存不会覆盖 `publishedVersionId`

### List Page Versions
GET /api/admin/sites/{siteId}/pages/{pageKey}/versions

Response fields:
- `versionId`
- `versionNo`
- `versionStatus`
- `versionNote`
- `createdBy`
- `createdAt`
- `publishedAt`
- `currentVersion`
- `publishedVersion`

Rules:
- 按 `versionNo DESC` 返回当前页面的历史版本
- 仅返回当前租户、当前站点、当前页面下的版本
- 版本列表可用于“回滚为新草稿”的选择面板

### Preview Page
POST /api/admin/sites/{siteId}/pages/{pageKey}/preview

Response fields:
- `pageKey`
- `versionId`
- `versionStatus`
- `runtimeConfig`
- `generatedAt`

Rules:
- 预览基于当前 `currentVersionId` 指向的版本生成
- `HOME` 的 `runtimeConfig` 是兼容 `site_homepage_config` 的运行时配置
- `PRODUCT / CHECKOUT / SUCCESS` 的 `runtimeConfig` 是编辑器预览模型，不会直接改写 storefront 模板

### Publish Page
POST /api/admin/sites/{siteId}/pages/{pageKey}/publish

Response fields:
- `pageKey`
- `publishedVersionId`
- `pageStatus`
- `versionStatus`
- `runtimeConfig`
- `publishedAt`

Rules:
- 发布会将当前版本标记为 `PUBLISHED`
- 如果此前存在其他已发布版本，会先归档旧版本
- `HOME` 页面发布后会同步回写 `site_homepage_config`
- `PRODUCT / CHECKOUT / SUCCESS` 页面发布后会更新 `page_layout_version.compiled_runtime_json` 和页面发布指针，但 storefront 仍沿用固定模板

### Rollback Page Version
POST /api/admin/sites/{siteId}/pages/{pageKey}/versions/{versionId}/rollback

Request:
```json
{
  "versionNote": "Rollback to baseline"
}
```

Rules:
- 回滚不会篡改历史版本，而是基于目标版本复制出一个新的 `DRAFT` 版本
- 如果当前已存在其他草稿版本，系统会先将旧草稿归档，再把新的回滚草稿设为 `currentVersionId`
- `publishedVersionId` 保持不变，直到用户再次显式发布

---

### List Site Templates
GET /site/template/list

Response:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "starter-one-product",
      "name": "Starter One Product",
      "category": "单品转化",
      "siteType": "LANDING",
      "previewImageUrl": null,
      "description": "聚焦单品转化，预置首屏卖点、倒计时和 FAQ 区块。",
      "builtIn": true,
      "createdAt": "2026-04-10T11:20:00"
    }
  ],
  "message": "OK"
}
```

Rules:
- 返回当前租户可见的模板，包含平台内置模板和租户私有模板
- 当前版本默认初始化内置模板，可直接用于自动建站表单

---

### Bind Domain
POST /api/admin/domains

Request:
```json
{
  "siteId": 1,
  "domain": "shop.example.com",
  "primary": true,
  "expiryAt": "2027-04-10T00:00:00"
}
```

Rules:
- 仅允许绑定当前租户下站点的域名
- 域名在全平台范围唯一
- 站点的第一条域名记录会被自动设为主域名
- 当 `primary=true` 时，会取消该站点其他域名的主域名标记

### List Site Domains
GET /api/admin/domains?siteId=1

Response:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "siteId": 1,
      "domain": "shop.example.com",
      "primary": true,
      "status": "ACTIVE",
      "expiryAt": null,
      "createdAt": "2026-04-10T12:00:00"
    }
  ],
  "message": "OK"
}
```

---

## Catalog APIs

### Create Category
POST /api/admin/categories

Request:
```json
{
  "name": "Summer Dresses",
  "slug": "summer-dresses"
}
```

Rules:
- `slug` 可省略，后端会基于 `name` 自动生成
- `slug` 在当前租户范围内唯一

### List Categories
GET /api/admin/categories

### Disable Category
POST /api/admin/categories/{id}/disable

### Enable Category
POST /api/admin/categories/{id}/enable

Rules:
- 仅允许操作当前租户下的分类
- 当前版本返回最小分类档案：`id`、`name`、`slug`、`status`

---

### Create Product
POST /api/admin/products

Request:
```json
{
  "sku": "SKU-001",
  "title": "Travel Backpack 40L",
  "categoryId": 1,
  "coverImage": "https://cdn.example.com/backpack-cover.jpg",
  "galleryImages": [
    "https://cdn.example.com/backpack-1.jpg",
    "https://cdn.example.com/backpack-2.jpg"
  ],
  "descriptionHtml": "<p>Water-resistant carry-on backpack.</p>",
  "sizes": ["20L", "40L"],
  "price": 59.90,
  "compareAtPrice": 79.90,
  "status": "DRAFT"
}
```

Rules:
- `sku` 在当前租户范围内唯一
- `categoryId` 必须属于当前租户可见分类
- `status` 可选，默认 `DRAFT`
- `compareAtPrice` 不能小于 `price`

### Update Product
PUT /api/admin/products/{id}

Rules:
- 仅允许更新当前租户下的商品
- 更新时会整体覆盖商品基础字段和 JSON 型图集/尺码数据

### List Products
GET /api/admin/products

Response fields include:
- `categoryName`
- `galleryImages`
- `sizes`

### Activate Product
POST /api/admin/products/{id}/activate

### Deactivate Product
POST /api/admin/products/{id}/deactivate

Rules:
- storefront 可见性现在由“商品主档 `ACTIVE` + `site_product_publish = PUBLISHED`”共同决定
- 商品被 `deactivate` 后，即使发布关系仍存在，也不会出现在首页 featured、分类页和商品详情页

---

### List Product Publish Matrix
GET /api/admin/products/{productId}/publishes

Response fields include:
- `siteId`
- `siteCode`
- `siteName`
- `siteDomain`
- `siteStatus`
- `productId`
- `productSku`
- `productTitle`
- `publishStatus`

Rules:
- 返回当前租户下所有站点的发布矩阵
- 若某站点还没有 `site_product_publish` 记录，也会返回一条 `publishStatus=UNPUBLISHED` 的派生结果，便于后台直接渲染发布状态

### Publish Product To Site
POST /api/admin/products/{productId}/publishes/{siteId}/publish

### Unpublish Product From Site
POST /api/admin/products/{productId}/publishes/{siteId}/unpublish

### Publish Product To All Sites
POST /api/admin/products/{productId}/publishes/publish-all

### Unpublish Product From All Sites
POST /api/admin/products/{productId}/publishes/unpublish-all

Rules:
- 仅允许操作当前租户下的商品和站点
- 发布关系按 `(tenant_id, site_id, product_id)` 唯一，重复发布会幂等更新为 `PUBLISHED`
- 未创建过发布关系的站点执行下架时，返回派生的 `UNPUBLISHED` 结果，不额外写记录

---

## Storefront Routes

### Storefront Home / Fallback
GET /

Rules:
- 公开路由，不需要登录
- 根据 `Host` 头解析已绑定且启用的站点
- 命中站点时，渲染 `storefront/home` 模板，并读取 `site_homepage_config` 中的 banner、menu、featured slots、theme color
- `featuredProductIds` 会按“`ACTIVE` + `site_product_publish = PUBLISHED`”过滤，只展示当前站点当前可见的商品
- 未命中或站点停用时，返回 `storefront/fallback` 模板和 `404`

### Storefront Category Listing
GET /category/{slug}

Query:
- `q`: 可选关键词，当前版本按商品标题和 SKU 模糊匹配

Rules:
- 公开路由，不需要登录
- 先按 `Host` 解析站点，再按 `slug` 解析当前租户下的分类
- `slug=all` 时返回当前站点所有当前可见商品
- 分类页只读取“`ACTIVE` + `site_product_publish = PUBLISHED`”的商品
- 若 `slug` 对应分类不存在或当前站点未命中，返回 `404`

### Storefront Product Detail
GET /product/{id}

Rules:
- 公开路由，不需要登录
- 先按 `Host` 解析站点，再按 `id` 查询当前站点当前可见商品
- 详情页渲染商品图集、规格、价格、划线价和 `descriptionHtml`
- 若商品未发布到当前站点、商品主档不是 `ACTIVE`、站点未命中，返回 `404`

### Storefront Cart
GET /cart

Rules:
- 公开路由，不需要登录
- 当前 demo 使用按 `siteId` 分桶的 server-side `HttpSession` 购物车，不落 `cart_session` 表
- 同一浏览器访问不同站点时，购物车会按站点隔离

### Add Cart Item
POST /cart/items

Form fields:
- `productId`
- `quantity`

Rules:
- 仅允许加入当前站点当前可见商品
- 若商品已在购物车中，则累加数量
- 成功后跳转到 `/cart`

### Update Cart Item Quantity
POST /cart/items/{productId}/quantity

Form fields:
- `quantity`

Rules:
- `quantity = 0` 视为移除
- 成功后跳转到 `/cart`

### Remove Cart Item
POST /cart/items/{productId}/remove

Rules:
- 成功后跳转到 `/cart`

### Storefront Checkout Form
GET /checkout

Rules:
- 从当前站点 cart session 读取商品摘要并展示结账表单
- 当前阶段 shipping 规则：`subtotal >= 100` 免运费，否则 `shipping = 9.90`
- 当前阶段 `tax = 0.00`

### Submit Checkout
POST /checkout

Form fields include:
- `firstName`
- `lastName`
- `email`
- `phone`
- `country`
- `state`
- `city`
- `postalCode`
- `addressLine1`

Rules:
- 仅当当前站点 cart session 非空时可提交
- 提交后创建 `orders` 和 `order_item`
- 提交成功后会初始化一条默认 `shipment_record`，初始状态为 `NOT_ORDERED` / `NOT_SHIPPED`
- 提交成功后会写入一条 `email_record`，并使用 mock sender 标记订单通知发送结果
- 成功后清空当前站点 cart session，并跳转到成功页

### Storefront Success Page
GET /order/{orderNo}/success

Rules:
- 公开路由，不需要登录
- 先按 `Host` 解析站点，再按 `orderNo` 查询当前站点订单
- 返回订单号、客户信息、金额摘要和基础状态
- 当订单 `paymentStatus = UNPAID` 时，页面提供 `Pay With Mock` 入口
- 当 Mock 支付完成后，成功页会展示 `PAID` 状态和已完成支付提示

### Initiate Storefront Payment
POST /payments/orders/{orderNo}/initiate

Form fields:
- `providerCode`

Rules:
- 公开路由，不需要登录
- 先按 `Host` 解析站点，再按 `orderNo` 查询当前站点订单
- 创建一条 `payment_record`，初始状态为 `PENDING`
- 若订单已支付，则直接跳回 `/order/{orderNo}/success`
- 当前 demo 默认使用 `providerCode = MOCK`
- `MOCK` 发起后跳转到 `/payments/mock/{paymentNo}`
- `PAYPAL` / `STRIPE` 目前仅保留 provider stub，尚未接真实实现

### Mock Payment Page
GET /payments/mock/{paymentNo}

Rules:
- 公开路由，不需要登录
- 仅 `providerCode = MOCK` 的支付记录可通过该页面访问
- 页面展示 `paymentNo`、`orderNo`、金额和当前支付状态

### Mock Payment Callback
POST /payments/mock/{paymentNo}/callback

Rules:
- 公开路由，不需要登录
- 模拟支付成功回调，写入 `payment_record.callback_payload`
- 将 `payment_record.status` 更新为 `SUCCEEDED`
- 将 `orders.payment_status` 与 `orders.order_status` 联动更新为 `PAID`
- 成功后跳转回 `/order/{orderNo}/success`

### Subsite Settings
GET /api/subsite/settings?siteId=1

Rules:
- 需要登录
- 仅允许读取当前租户下的站点设置
- 返回站点主档字段与 `site_setting.default_config_json` 中的安全配置子集
- 当前阶段 `siteUrl` 只读展示，实际域名调整仍通过 `/api/admin/domains` 处理

### Update Subsite Settings
PUT /api/subsite/settings

Body fields:
- `siteId`
- `siteName`
- `supportEmail`
- `supportPhone`
- `whatsapp`
- `facebook`
- `currencyCode`
- `countryCode`
- `languageCode`
- `logisticsText`
- `logoUrl`
- `bannerTitle`
- `bannerSubtitle`

Rules:
- 需要登录
- 仅允许更新当前租户站点
- `siteName` / `currencyCode` / `countryCode` / `languageCode` 若显式传入则不能为空白
- 站点主档同步更新 `siteName`、币种、国家、语言、logo、banner 字段
- 联系方式与物流文案写入 `site_setting.default_config_json`

### Subsite Orders
GET /api/subsite/orders?siteId=1

Query params:
- `siteId` required
- `orderNo` optional
- `orderStatus` optional
- `paymentStatus` optional
- `createdFrom` optional, format `YYYY-MM-DD`
- `createdTo` optional, format `YYYY-MM-DD`

Rules:
- 需要登录
- 仅允许查询当前租户下指定站点的订单
- 返回当前站点订单摘要列表，不跨站点聚合
- 当前阶段支持按订单号、订单状态、支付状态和日期区间筛选

### Supply Shipments
GET /api/supply/shipments

Query params:
- `orderNo` optional
- `trackingNo` optional
- `customerEmail` optional

Rules:
- 需要登录
- 仅查询当前租户下的订单与发货记录
- 支持按订单号、物流号、客户邮箱检索
- 若某订单尚未更新过物流信息，仍会返回默认状态 `NOT_ORDERED` / `NOT_SHIPPED`

### Update Supply Shipment
PUT /api/supply/shipments/{orderNo}

Body fields:
- `procurementStatus` optional, one of `NOT_ORDERED` / `ORDERED` / `FAILED`
- `shipmentStatus` optional, one of `NOT_SHIPPED` / `SHIPPED` / `DELIVERED` / `EXCEPTION`
- `trackingNo` optional
- `carrier` optional
- `failureReason` optional

Rules:
- 需要登录
- 仅允许更新当前租户下的订单发货记录
- 若订单还没有 `shipment_record`，会先创建默认记录再更新
- 成功后会同步回写 `orders.shipping_status`

### Admin Reports
GET /api/admin/reports

Query params:
- `siteId` optional
- `dateFrom` optional, format `YYYY-MM-DD`
- `dateTo` optional, format `YYYY-MM-DD`

Rules:
- 需要登录
- 返回当前租户下的汇总指标：`totalOrders`、`paidOrders`、`shippedOrders`、`totalRevenue`
- `totalRevenue` 当前按已支付订单金额汇总
- 同时返回 `siteSummaries`，按站点聚合订单数、已支付数、已发货数和收入
- 支持按站点和日期区间过滤

---

### Test Connection (IMPORTANT)
GET /site/test?id=1

Call:
GET /wp-json/wp/v2/users/me

---

### List Sites
GET /site/list

---

### Site Workspace
GET /site/workspace?id=1

Response:
```json
{
  "success": true,
  "data": {
    "siteId": 1,
    "tenantId": 1001,
    "workspaceStatus": "ACTION_REQUIRED",
    "generatedAt": "2026-04-10T10:40:00",
    "profile": {
      "siteId": 1,
      "name": "Workspace Site",
      "siteType": "REGISTERED",
      "domain": "https://workspace.example",
      "baseUrl": "https://workspace.example",
      "adminUrl": "https://workspace.example/wp-admin",
      "status": "ACTIVE",
      "provisionStatus": "NONE",
      "statusMessage": "Connection successful",
      "templateCode": null,
      "templateName": null,
      "countryCode": null,
      "languageCode": null,
      "currencyCode": null,
      "createdAt": "2026-04-10T10:30:00",
      "createdBy": null
    },
    "readiness": {
      "score": 20,
      "level": "NOT_READY",
      "items": [
        {
          "code": "SITE_ACCESSIBLE",
          "label": "站点可访问",
          "status": "DONE",
          "message": "站点当前可访问，可继续进入运营配置",
          "action": "OPEN_SITE"
        }
      ]
    },
    "moduleSummaries": [],
    "pendingTasks": [],
    "alerts": [],
    "recentActivities": [],
    "quickActions": []
  },
  "message": "OK"
}
```

Rules:
- 仅允许读取当前租户下的站点工作台
- 首版聚合站点档案、准备度、模块摘要、待处理任务、告警、最近活动和快捷动作
- `workspaceStatus` 可能值：`CREATING / ACTIVE / ACTION_REQUIRED / AT_RISK / DISABLED / ARCHIVED`

---

## Post APIs

### Create Post
POST /post/create

---

### List Posts
GET /post/list

---

## Publish APIs (CORE)

### Publish Post
POST /publish

Request:
{
"postId": 1,
"siteIds": [1,2,3]
}

Response:
```json
{
  "success": true,
  "data": {
    "postId": 1,
    "totalSites": 2,
    "results": [
      {
        "publishId": 10,
        "siteId": 1,
        "status": "PENDING",
        "message": "Queued for execution",
        "retryCount": 0,
        "remotePostId": null,
        "remotePostUrl": null
      },
      {
        "publishId": 11,
        "siteId": 2,
        "status": "PENDING",
        "message": "Queued for execution",
        "retryCount": 0,
        "remotePostId": null,
        "remotePostUrl": null
      }
    ]
  },
  "message": "OK"
}
```

### Publish Records
GET /publish/list

Response:
```json
{
  "success": true,
  "data": [
    {
      "publishId": 10,
      "postId": 1,
      "postTitle": "Hello World",
      "siteId": 2,
      "siteName": "Main Site",
      "status": "SUCCESS",
      "targetStatus": "publish",
      "retryCount": 0,
      "lastHttpStatus": 201,
      "message": "Publish successful",
      "remotePostId": 101,
      "remotePostUrl": "https://example.com/?p=101",
      "createdAt": "2026-04-09T09:20:00"
    }
  ],
  "message": "OK"
}
```

---

## Internal Flow (IMPORTANT)

PublishService:
- loop siteIds
- create `PENDING` records
- hand off to async executor

Worker:
- load queued record
- mark `PROCESSING`
- call WP API
- save result to `post_publish`
- immediate retry up to 3 times for retryable failures

---

## WordPress API

Endpoint:
POST /wp-json/wp/v2/posts

Headers:
Authorization: Basic base64(username:app_password)

Body:
{
"title": "...",
"content": "...",
"status": "publish"
}

---

## Error Handling

Must handle:
- 401 → auth failed
- timeout → retry
- 5xx → retry

Retry max: 3 times

---

## Rules for Codex

- Always include tenant_id
- Never bypass service layer
- Do NOT write raw HTTP in controller
- Use WpClient abstraction
