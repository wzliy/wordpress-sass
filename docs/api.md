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

Request:
{
"name": "My Blog",
"baseUrl": "https://example.com",
"wpUsername": "admin",
"appPassword": "xxxx"
}

### Provision Site
POST /site/provision

Request:
{
"name": "Tenant Blog",
"adminEmail": "owner@example.com",
"subdomainPrefix": "tenant-blog"
}

---

### Test Connection (IMPORTANT)
GET /site/test?id=1

Call:
GET /wp-json/wp/v2/users/me

---

### List Sites
GET /site/list

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
