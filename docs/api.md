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

## Storefront Routes

### Storefront Home / Fallback
GET /

Rules:
- 公开路由，不需要登录
- 根据 `Host` 头解析已绑定且启用的站点
- 命中站点时，渲染 `storefront/home` 模板，并读取 `site_homepage_config` 中的 banner、menu、featured slots、theme color
- 未命中或站点停用时，返回 `storefront/fallback` 模板和 `404`

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
