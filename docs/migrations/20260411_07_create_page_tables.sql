CREATE TABLE IF NOT EXISTS page (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    page_key VARCHAR(40) NOT NULL,
    page_name VARCHAR(120) NOT NULL,
    page_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_version_id BIGINT,
    published_version_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS page_layout_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    page_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    version_status VARCHAR(20) NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    layout_json TEXT,
    compiled_runtime_json TEXT,
    version_note VARCHAR(255),
    created_by VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME NULL
);

CREATE UNIQUE INDEX uk_page_tenant_site_key ON page(tenant_id, site_id, page_key);
CREATE INDEX idx_page_tenant_site_status ON page(tenant_id, site_id, status);
CREATE UNIQUE INDEX uk_page_layout_version_tenant_page_no ON page_layout_version(tenant_id, page_id, version_no);
CREATE INDEX idx_page_layout_version_tenant_site_page ON page_layout_version(tenant_id, site_id, page_id);
