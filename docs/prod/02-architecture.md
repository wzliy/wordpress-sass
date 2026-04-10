# 02 - Architecture and Data Model

## 1. Target architecture
Single-server modular monolith.

```text
[Nginx]
  -> [Spring Boot App]
       -> admin APIs
       -> storefront controllers
       -> sub-site APIs
       -> supply-chain APIs
       -> MySQL
```

## 2. Runtime flow
### 2.1 Admin creates site
Admin API -> persist site -> persist domain -> persist default homepage config -> site becomes active.

### 2.2 Customer visits domain
Nginx forwards request with original `Host` -> Spring Boot resolves domain -> loads site config -> renders storefront.

### 2.3 Customer places order
Storefront checkout -> order created -> payment attempt created -> mock payment completes -> email event emitted -> order visible in reports and supply-chain console.

## 3. Suggested modules
- `modules/common`: enums, exceptions, shared response model, utilities
- `modules/site`: site and settings
- `modules/domain`: domain mapping and host resolution
- `modules/product`: categories, products, publish rules
- `modules/storefront`: public storefront controllers/views
- `modules/order`: cart snapshot, order creation, order items, order state
- `modules/payment`: provider abstraction and payment records
- `modules/email`: sender abstraction and template hooks
- `modules/subsite`: site-level settings and site order query
- `modules/shipping`: procurement and shipping tracking states
- `modules/report`: aggregate reports
- `modules/admin`: admin controllers/application services

## 4. Minimal database tables
### 4.1 site
- id
- site_code
- site_name
- template_code
- currency
- language
- country
- status
- theme_color
- logo_url
- banner_title
- banner_subtitle
- created_at
- updated_at

### 4.2 site_domain
- id
- site_id
- domain
- is_primary
- status
- expiry_at
- created_at

### 4.3 site_homepage_config
- id
- site_id
- config_json
- created_at
- updated_at

### 4.4 category
- id
- name
- slug
- status

### 4.5 product
- id
- sku
- title
- category_id
- cover_image
- gallery_json
- description_html
- sizes_json
- price
- compare_at_price
- status
- created_at
- updated_at

### 4.6 site_product_publish
- id
- site_id
- product_id
- publish_status
- created_at
- updated_at

### 4.7 cart_session (optional, if persisted)
- id
- site_id
- session_token
- cart_json
- updated_at

### 4.8 orders
- id
- site_id
- order_no
- customer_first_name
- customer_last_name
- customer_email
- customer_phone
- country
- state
- city
- address_line1
- postal_code
- currency
- subtotal_amount
- shipping_amount
- tax_amount
- total_amount
- order_status
- payment_status
- shipping_status
- created_at
- updated_at

### 4.9 order_item
- id
- order_id
- product_id
- sku
- product_title
- size_value
- quantity
- unit_price
- line_total

### 4.10 payment_record
- id
- order_id
- provider_code
- payment_no
- amount
- currency
- status
- callback_payload
- created_at
- updated_at

### 4.11 email_record
- id
- order_id
- template_code
- recipient
- status
- response_message
- created_at

### 4.12 shipment_record
- id
- order_id
- procurement_status
- shipment_status
- tracking_no
- carrier
- failure_reason
- updated_at

## 5. Key enums
### SiteStatus
- INIT
- ACTIVE
- DISABLED

### ProductStatus
- DRAFT
- ACTIVE
- INACTIVE

### PublishStatus
- PUBLISHED
- UNPUBLISHED

### OrderStatus
- CREATED
- PAID
- CANCELLED
- FULFILLING
- COMPLETED

### PaymentStatus
- PENDING
- SUCCESS
- FAILED

### ShippingStatus
- NOT_SHIPPED
- SHIPPED
- DELIVERED
- EXCEPTION

## 6. API groups
### Admin APIs
- `/api/admin/sites`
- `/api/admin/domains`
- `/api/admin/categories`
- `/api/admin/products`
- `/api/admin/reports`

### Storefront routes
- `GET /`
- `GET /category/{slug}`
- `GET /product/{id}`
- `GET /cart`
- `POST /cart/items`
- `POST /checkout`
- `GET /order/{orderNo}/success`

### Sub-site APIs
- `/api/subsite/settings`
- `/api/subsite/orders`

### Supply-chain APIs
- `/api/supply/orders`
- `/api/supply/shipments`

## 7. Demo seed data
Must include:
- 1 default site,
- 1 default bound domain for local testing (for example mapped via hosts file),
- 3 categories,
- 6 products,
- 2 featured products,
- 1 sample order,
- 1 sample shipment record.
