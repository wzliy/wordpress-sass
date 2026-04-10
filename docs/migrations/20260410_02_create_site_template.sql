CREATE TABLE IF NOT EXISTS site_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50),
    site_type VARCHAR(30),
    preview_image_url VARCHAR(255),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_builtin TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_site_template_tenant_status ON site_template(tenant_id, status);
CREATE UNIQUE INDEX uk_site_template_tenant_code ON site_template(tenant_id, code);
