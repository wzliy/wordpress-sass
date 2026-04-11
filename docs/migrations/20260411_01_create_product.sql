CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    category_id BIGINT NOT NULL,
    cover_image VARCHAR(255),
    gallery_json TEXT,
    description_html TEXT,
    sizes_json TEXT,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_product_tenant_sku ON product(tenant_id, sku);
CREATE INDEX idx_product_tenant_category_status ON product(tenant_id, category_id, status);
