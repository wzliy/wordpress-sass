# SQL Migration Guidelines

当前仓库继续使用 `docs/migrations/*.sql` 作为稳定迁移约定，不在本阶段切换到 Flyway / Liquibase。

## Naming

- 文件名格式：`YYYYMMDD_NN_short_description.sql`
- 示例：`20260411_06_create_shipment_record.sql`
- `NN` 为当天顺序号，保持两位数

## Authoring Rules

- 每个迁移只做一个清晰的结构变更主题，避免把无关表混在同一文件里
- 新业务表默认补 `tenant_id`
- 新增索引与表结构放在同一迁移内
- 不在迁移里写测试数据或演示数据
- 变更用户可见接口时，同时更新 `docs/api.md`
- 变更任务完成状态时，同时更新 `docs/plan.md`

## Execution Order

1. 按文件名升序执行
2. 先执行历史未落库迁移，再启动应用
3. 新环境可直接参考 `docs/database.sql` 初始化，再以增量迁移保持同步

## Roll-forward Rule

- 默认只做前滚，不写回滚脚本
- 若线上/共享环境发现问题，新增修复迁移，不直接改写已发布迁移文件

## Current Scope

当前已采用该约定的主线结构包括：

- `site` / `site_domain` / `site_homepage_config`
- `category` / `product` / `site_product_publish`
- `orders` / `order_item`
- `payment_record`
- `email_record`
- `shipment_record`
