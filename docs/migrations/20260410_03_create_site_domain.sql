CREATE TABLE IF NOT EXISTS site_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    domain VARCHAR(255) NOT NULL,
    is_primary TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expiry_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_site_domain_tenant_site ON site_domain(tenant_id, site_id);
CREATE UNIQUE INDEX uk_site_domain_domain ON site_domain(domain);
