CREATE TABLE site_product_publish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    publish_status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_site_product_publish_tenant_site_product ON site_product_publish(tenant_id, site_id, product_id);
CREATE INDEX idx_site_product_publish_tenant_product_status ON site_product_publish(tenant_id, product_id, publish_status);
CREATE INDEX idx_site_product_publish_tenant_site_status ON site_product_publish(tenant_id, site_id, publish_status);
