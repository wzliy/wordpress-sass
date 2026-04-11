# Demo Runbook

## Seed Scope

`scripts/demo-seed.sh` creates or reuses the following demo assets:

- 1 storefront site: `Demo Shop`
- 1 local demo domain: `demo-shop.local`
- 3 categories: `travel-bags`, `wellness`, `home-fitness`
- 3 published products
- 2 demo orders:
  - 1 paid + shipped order
  - 1 unpaid + not-shipped order
- 1 shipment tracking update for the paid order

Site, category and product creation are idempotent. Re-running the script appends new demo orders while reusing existing catalog assets.

## Prerequisites

1. Prepare MySQL and initialize schema with `docs/database.sql` or ordered files in `docs/migrations/`.
2. Ensure an admin account exists.
   Default local path:
   - bootstrap credentials: `admin / admin123`
   - or run `docs/init-admin.sql`
3. Start the app:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

4. Verify health:

```bash
curl http://127.0.0.1:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## Local Hosts

For browser-based storefront verification, add this entry to your local hosts file:

```text
127.0.0.1 demo-shop.local
```

The seed script itself does not require the hosts entry because it sends the `Host` header directly. The hosts mapping is only for browser access.

## Run Seed

```bash
bash scripts/demo-seed.sh
```

Useful environment overrides:

```bash
APP_URL=http://127.0.0.1:8080
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
STORE_HOST=demo-shop.local
STORE_BASE_URL=http://demo-shop.local:8080
```

## Expected Output

The script prints:

- `siteId`
- created/reused category ids
- created/reused product ids
- one paid order number
- one pending order number
- compact report summary

## Manual Verification Path

1. Open storefront home:

```text
http://demo-shop.local:8080/
```

2. Open catalog:

```text
http://demo-shop.local:8080/category/all
```

3. Verify seeded order/report paths with the order numbers printed by the script:

- success page: `http://demo-shop.local:8080/order/{orderNo}/success`
- supply query: `GET /api/supply/shipments?orderNo={orderNo}`
- admin report: `GET /api/admin/reports`

## Notes

- The script uses current APIs instead of direct SQL so it also acts as a lightweight integration seed.
- If you want a completely clean dataset, recreate the database before re-running the seed.
- Product images use placeholder CDN URLs for demo rendering only.
