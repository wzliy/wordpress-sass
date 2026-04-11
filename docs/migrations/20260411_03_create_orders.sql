CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    order_no VARCHAR(40) NOT NULL,
    customer_first_name VARCHAR(80) NOT NULL,
    customer_last_name VARCHAR(80) NOT NULL,
    customer_email VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(40),
    country VARCHAR(10) NOT NULL,
    state VARCHAR(80),
    city VARCHAR(80) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    postal_code VARCHAR(40) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    subtotal_amount DECIMAL(10,2) NOT NULL,
    shipping_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    shipping_status VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_title VARCHAR(160) NOT NULL,
    size_value VARCHAR(40),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL
);

CREATE UNIQUE INDEX uk_orders_order_no ON orders(order_no);
CREATE INDEX idx_orders_tenant_site_created ON orders(tenant_id, site_id, created_at);
CREATE INDEX idx_orders_tenant_status ON orders(tenant_id, order_status, payment_status, shipping_status);
CREATE INDEX idx_order_item_tenant_order ON order_item(tenant_id, order_id);
