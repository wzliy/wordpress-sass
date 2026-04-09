# WordPress Multisite Provision Design

## Goal
Use WordPress Multisite as the automatic site provisioning backend for `/site/provision`.

## Key Constraint
WordPress core provides multisite initialization functions such as `wp_initialize_site()`, but site creation is not exposed as a stable built-in REST endpoint for SaaS orchestration.

Therefore this project uses:
- Spring Boot as the SaaS control plane
- A custom WordPress Multisite admin plugin endpoint as the provisioning gateway

## Provision Flow
1. SaaS calls `POST /site/provision`
2. Backend stores a `PROVISIONING` site record
3. `MultisiteSiteProvisioner` calls the Multisite admin endpoint
4. WordPress plugin creates the subsite and admin user
5. Plugin returns:
   - `baseUrl`
   - `domain`
   - `adminUrl`
   - `wpUsername`
   - `appPassword`
6. SaaS updates local `site` record to `ACTIVE`

## Required WordPress Endpoint
Recommended endpoint:

`POST /wp-json/wpss/v1/sites`

Request body:
```json
{
  "title": "Tenant Blog",
  "slug": "tenant-blog",
  "adminEmail": "owner@example.com",
  "tenantId": 1001
}
```

## Plugin Prototype
仓库已提供可安装原型：

- `wordpress-plugin/wpss-multisite-provisioner/wpss-multisite-provisioner.php`

该插件会：
- 校验调用方具备 `manage_network`
- 按 Multisite 模式创建子站
- 复用或创建管理员用户
- 把管理员加入子站并授予 `administrator`
- 生成并返回 Application Password

安装说明见：

- `wordpress-plugin/wpss-multisite-provisioner/README.md`

Response body:
```json
{
  "baseUrl": "https://tenant-blog.example.com",
  "domain": "https://tenant-blog.example.com",
  "adminUrl": "https://tenant-blog.example.com/wp-admin",
  "wpUsername": "admin_1001",
  "appPassword": "xxxx xxxx xxxx xxxx",
  "message": "Multisite provision completed"
}
```

## Spring Boot Config
Use these environment variables:

- `WP_MULTISITE_ENABLED`
- `WP_MULTISITE_BASE_URL`
- `WP_MULTISITE_PROVISION_ENDPOINT`
- `WP_MULTISITE_ADMIN_USERNAME`
- `WP_MULTISITE_ADMIN_APP_PASSWORD`
- `WP_MULTISITE_NETWORK_DOMAIN`
- `WP_MULTISITE_USE_HTTPS`

## Current Fallback
If `WP_MULTISITE_ENABLED=false`, the project uses a mock provisioner for local development.
