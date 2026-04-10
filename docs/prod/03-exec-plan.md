# 03 - Exec Plan for Codex

This file is the execution plan Codex must follow.

## Phase 0 - Bootstrap repository
### Tasks
- Initialize Spring Boot 3.x project with Java 17 and Maven.
- Add modules/package skeleton.
- Add base configuration profiles: `local`, `dev`.
- Add Flyway or SQL init scripts.
- Add common response wrapper, exception handling, enum package.

### Deliverables
- Project compiles.
- App starts.
- Health endpoint available.

### Acceptance criteria
- `mvn test` passes.
- `mvn spring-boot:run` starts successfully.
- `/actuator/health` returns UP.

---

## Phase 1 - Site + domain core
### Tasks
- Implement `site` and `site_domain` tables.
- Implement admin APIs:
  - create site,
  - list sites,
  - get site detail,
  - enable/disable site,
  - bind domain.
- Implement host/domain resolver service.
- Add site-not-found fallback page.

### Acceptance criteria
- Creating a site stores site and domain.
- Host resolver can resolve active domains.
- Unknown host returns fallback page.
- Tests cover site creation and domain uniqueness.

### Manual verification
1. create a site via API,
2. add local hosts entry,
3. visit domain,
4. see fallback or target behavior as expected.

---

## Phase 2 - Homepage config + storefront home
### Tasks
- Implement `site_homepage_config` table.
- Seed default homepage config on site creation.
- Create Thymeleaf storefront home page.
- Render site-specific logo, banner, menu, featured products from config.

### Acceptance criteria
- Visiting `/` on a resolved domain renders the correct site homepage.
- Different domains can render different homepage content.

### Manual verification
1. create 2 sites,
2. bind 2 domains,
3. confirm each domain renders different title/banner.

---

## Phase 3 - Category + product core
### Tasks
- Implement categories and products tables.
- Implement admin product APIs:
  - create product,
  - update product,
  - list products,
  - set active/inactive.
- Implement `site_product_publish` table.
- Implement central publish/unpublish APIs per site or all sites.
- Seed demo categories/products.

### Acceptance criteria
- Admin can CRUD products.
- Publish/unpublish state affects storefront visibility.
- Storefront homepage featured product section only shows published products.

### Manual verification
1. publish a product,
2. refresh storefront,
3. confirm visibility,
4. unpublish it,
5. confirm it disappears.

---

## Phase 4 - Category page + product detail page
### Tasks
- Implement category/list route.
- Implement search by keyword.
- Implement product detail route.
- Render gallery, sizes, price, compare-at price.

### Acceptance criteria
- Users can browse categories.
- Users can search products.
- Product detail page renders complete data.

---

## Phase 5 - Cart + checkout + order creation
### Tasks
- Implement cart service.
- Decide cart persistence strategy for demo:
  - session-based or cookie-based.
- Implement cart page.
- Implement checkout form.
- Implement orders and order_item tables.
- Create order on checkout.
- Calculate subtotal, shipping, tax, total using simple demo rules.

### Acceptance criteria
- Customer can add/update/remove cart items.
- Customer can submit checkout.
- Order and order items are stored.
- Success page shows order number.

### Manual verification
1. add product to cart,
2. update quantity,
3. checkout,
4. see success page,
5. verify order in DB.

---

## Phase 6 - Payment abstraction
### Tasks
- Implement `payment_record` table.
- Define payment provider interface.
- Implement mock payment provider.
- Create stub provider classes for PayPal and Stripe with TODO markers.
- Add payment initiation and callback endpoints.

### Acceptance criteria
- Mock payment can mark an order as paid.
- Payment records are persisted.
- Order payment status updates after success.

### Manual verification
1. checkout,
2. choose mock payment,
3. simulate callback,
4. verify order status becomes paid.

---

## Phase 7 - Email abstraction
### Tasks
- Implement `email_record` table.
- Define email sender interface.
- Implement mock sender.
- Trigger order-placed email event.

### Acceptance criteria
- Successful order placement creates email record.
- Mock sender marks send result.

---

## Phase 8 - Sub-site admin basics
### Tasks
- Implement sub-site settings API.
- Implement sub-site order query API.
- Restrict editable fields to allowed subset.
- Add minimal admin pages or JSON APIs.

### Acceptance criteria
- Sub-site operator can read/update site settings.
- Sub-site operator can query only current-site orders.

---

## Phase 9 - Supply-chain query console
### Tasks
- Implement `shipment_record` table.
- Add procurement/shipping statuses.
- Build APIs to query by order number, tracking number, email.
- Add API to update shipment state for demo/admin use.

### Acceptance criteria
- Shipment state can be queried.
- Orders display procurement and shipping status.

---

## Phase 10 - Aggregate reports
### Tasks
- Build admin report APIs for:
  - total orders,
  - paid orders,
  - shipped orders,
  - revenue,
  - per-site summary.
- Add date-range filters.

### Acceptance criteria
- Reports return correct aggregated numbers from seed and created orders.
- Tests validate calculations.

---

## Phase 11 - Final hardening for demo
### Tasks
- Improve error handling and validation.
- Add seed/demo script and run instructions.
- Add example `hosts` setup instructions.
- Add screenshots or sample curl commands in README.
- Update status log fully.

### Acceptance criteria
- Fresh setup can run locally.
- Demo path is documented end to end.
- All previous phase checks still pass.
