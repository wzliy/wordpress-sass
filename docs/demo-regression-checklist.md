# Demo Regression Checklist

## Bootstrap

1. Apply schema and migration SQL to an empty database.
2. Start the backend with `local` profile.
3. Confirm `GET /actuator/health` returns `UP`.
4. Ensure admin login works with `admin / admin123`.

## Seed

1. Run `bash scripts/demo-seed.sh`.
2. Confirm the script prints:
   - `siteId`
   - paid order number
   - pending order number
   - report summary

## Storefront

1. Add `127.0.0.1 demo-shop.local` to local hosts.
2. Open `http://demo-shop.local:8080/`.
3. Open `http://demo-shop.local:8080/category/all`.
4. Open one seeded product detail page from the category page.

## Order Flow

1. Create a new cart from storefront.
2. Submit checkout.
3. Confirm success page renders order number and `UNPAID`.
4. Click `Pay With Mock`.
5. Confirm payment callback returns to success page with `PAID`.

## Supply

1. Query `GET /api/supply/shipments?orderNo={paidOrderNo}`.
2. Confirm shipment is visible with current procurement and shipment states.
3. Update `PUT /api/supply/shipments/{orderNo}` to `DELIVERED`.
4. Confirm `orders.shipping_status` and shipment query both reflect the update.

## Subsite Ops

1. Query `GET /api/subsite/settings?siteId={siteId}`.
2. Update `PUT /api/subsite/settings` with support/contact fields.
3. Query `GET /api/subsite/orders?siteId={siteId}` and verify seeded orders are visible.

## Reports

1. Query `GET /api/admin/reports`.
2. Confirm totals include seeded orders.
3. Query `GET /api/admin/reports?siteId={siteId}`.
4. Confirm `siteSummaries` and aggregate counts match the seeded state.

## Close-out

1. Confirm migration files and `docs/database.sql` are still aligned.
2. Re-run targeted backend smoke tests if any backend code changed after seeding support updates.
