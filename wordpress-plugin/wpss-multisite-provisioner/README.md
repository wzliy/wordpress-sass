# WPSS Multisite Provisioner

这个插件为 WordPress Multisite 提供一个受保护的 REST 接口：

- `POST /wp-json/wpss/v1/sites`

它用于给当前项目的 Spring Boot 控制面创建子站，并返回：

- `baseUrl`
- `domain`
- `adminUrl`
- `wpUsername`
- `appPassword`

## 安装方式

1. 把整个 `wpss-multisite-provisioner` 目录复制到 WordPress 的 `wp-content/plugins/`
2. 在网络后台启用这个插件
3. 确保当前 WordPress 已启用 Multisite
4. 确保网络管理员账号已创建 Application Password
5. 在 Spring Boot 中配置：

```bash
WP_MULTISITE_ENABLED=true
WP_MULTISITE_BASE_URL=https://network.example.com
WP_MULTISITE_PROVISION_ENDPOINT=/wp-json/wpss/v1/sites
WP_MULTISITE_ADMIN_USERNAME=network-admin
WP_MULTISITE_ADMIN_APP_PASSWORD=xxxx xxxx xxxx xxxx
```

## 请求示例

```bash
curl -X POST https://network.example.com/wp-json/wpss/v1/sites \
  -u 'network-admin:xxxx xxxx xxxx xxxx' \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Tenant Blog",
    "slug": "tenant-blog",
    "adminEmail": "owner@example.com",
    "tenantId": 1001
  }'
```

## 行为说明

- 仅 `manage_network` 权限用户可以调用
- 同邮箱管理员已存在时会直接复用，不会重复创建用户
- 会把管理员加入新站点并授予 `administrator`
- 会为该管理员生成新的 Application Password 并在响应中返回
- 会把 `tenantId` 写入站点选项 `wpss_tenant_id`

## 当前边界

- 这是原型版本，尚未加入建站失败回滚
- Application Password 通常要求 HTTPS 环境
- 若同一管理员多次建站，会生成多条不同的 Application Password 记录
