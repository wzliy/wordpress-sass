# 01 - Product Requirements Document (PRD)

## 1. Product summary
A platform to manage multiple cross-border ecommerce independent sites from one central backend. The platform manages domains, site records, product publishing, storefront rendering, customer ordering, payment abstraction, email notifications, order aggregation, and basic supply-chain shipping queries.

## 2. Roles
### 2.1 Platform admin
Can:
- create and manage sub-sites,
- bind domains,
- manage central products,
- control product publish/unpublish to sites,
- view aggregate order/sales reports,
- configure payment/email/shipping defaults.

### 2.2 Sub-site operator
Can:
- edit sub-site basic settings,
- view site orders,
- configure homepage/theme subset,
- manage coupons/shipping/payment display options within allowed scope.

### 2.3 Supply-chain operator
Can:
- query order shipment status,
- mark procurement status,
- track shipping success/failure.

### 2.4 Customer
Can:
- browse categories and products,
- search products,
- view product details,
- add to cart,
- checkout,
- pay,
- receive email notifications.

## 3. Scope for demo release
### In scope
1. Multi-tenant site resolution by request host/domain.
2. Platform admin APIs for site creation and central product management.
3. Central publish/unpublish switch that affects storefront visibility.
4. Storefront pages:
   - home,
   - category/list,
   - product detail,
   - cart,
   - checkout,
   - order confirmation.
5. Order creation and basic order lifecycle states.
6. Payment provider abstraction with mock provider and pluggable PayPal/Stripe adapters.
7. Email abstraction with mock sender and event hook for order placed.
8. Sub-site basic settings.
9. Supply-chain query pages/APIs for procurement/shipping statuses.
10. Basic aggregate reports.

### Out of scope for demo
1. Real registrar integration.
2. Real OA approval workflow.
3. Auto SSL issuance.
4. Full CMS and visual drag-and-drop editor.
5. Full coupon/marketing engine.
6. Full cloak implementation.
7. Real ERP/WMS deep integration.

## 4. Functional requirements

## 4.1 Platform admin backend
### 4.1.1 Site management
- Create site with:
  - site name,
  - site code,
  - primary domain,
  - template code,
  - default currency,
  - default language,
  - site status.
- List sites.
- View site detail.
- Enable/disable site.
- Edit site basic metadata.

### 4.1.2 Domain management
- Bind a domain to a site.
- Mark one domain as primary.
- Validate uniqueness of domain.
- Track domain status (`PENDING`, `ACTIVE`, `DISABLED`).
- Record expiry date as metadata only.

### 4.1.3 Product management
- Create/edit/delete product.
- Product fields for demo:
  - SKU,
  - title,
  - cover image,
  - gallery images,
  - description,
  - category,
  - sizes,
  - price,
  - compare-at price,
  - status.
- Bulk import is optional for demo; if implemented, CSV import is enough.
- Central publish/unpublish to sites.

### 4.1.4 Reporting
- Aggregate orders and sales across all sites.
- Filter by date range and site.
- Count total orders, paid orders, shipped orders, total revenue.

## 4.2 Storefront
### 4.2.1 Home page
- Render site-specific logo, theme color, banner, menu, featured products.
- Desktop and mobile-friendly layout.

### 4.2.2 Category/list page
- Display products by category.
- Search by keyword.
- Paginate if needed.

### 4.2.3 Product detail page
- Show title, gallery, price, compare-at price, description, size selector, quantity selector.
- Add to cart.

### 4.2.4 Cart
- View items, update quantity, delete item.
- Calculate subtotal.

### 4.2.5 Checkout
- Collect customer info:
  - first name,
  - last name,
  - email,
  - phone,
  - address line,
  - city,
  - state/province,
  - postal code,
  - country.
- Show shipping fee, tax, grand total.
- Create order.

### 4.2.6 Payment
- Payment abstraction layer.
- Demo must support:
  - mock payment provider,
  - stub adapters for PayPal and Stripe.
- Record payment attempt and callback result.

### 4.2.7 Email
- Trigger order-placed email event.
- Use mock sender in demo by default.

## 4.3 Sub-site backend
### 4.3.1 Basic settings
Editable fields:
- site title,
- site url,
- support email,
- support phone,
- WhatsApp,
- Facebook,
- currency,
- country,
- language,
- logistics text/settings,
- logo,
- banner title/subtitle.

### 4.3.2 Order query
- Query current site orders.
- Filter by order number, date, status.

### 4.3.3 Homepage/theme subset
- For demo, allow only limited editable homepage config:
  - banner title,
  - banner subtitle,
  - theme color,
  - featured product ids,
  - menu labels.

## 4.4 Supply-chain console
### 4.4.1 Order procurement state
States:
- `NOT_ORDERED`
- `ORDERED`
- `FAILED`

### 4.4.2 Shipping state
States:
- `NOT_SHIPPED`
- `SHIPPED`
- `DELIVERED`
- `EXCEPTION`

### 4.4.3 Shipping query
- Query by order number / tracking number / customer email.
- View latest shipment state.

## 5. Business rules
1. Product source of truth is central admin.
2. Product publish/unpublish is controlled centrally.
3. Orders belong to one site but are reportable centrally.
4. Site rendering is based on request host.
5. If a domain is not found or inactive, return a site-not-found page.
6. Payment and email must be provider-based abstractions.
7. Advanced cloaking behavior is excluded from demo implementation.

## 6. Non-functional requirements
- Single server deployment.
- Modular monolith.
- Seed demo data included.
- Core flows covered by tests.
- Configurable through application profiles.

## 7. Difficulty assessment
### Overall
- **Demo complexity:** medium-high
- **Full product complexity:** high

### Hardest parts for implementation
1. multi-tenant site resolution,
2. central publish rules,
3. order/payment state machine,
4. aggregate reporting,
5. keeping scope controlled.
