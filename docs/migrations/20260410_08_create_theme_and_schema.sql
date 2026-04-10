CREATE TABLE IF NOT EXISTS theme_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    config_scope VARCHAR(20) NOT NULL,
    tokens_json TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_theme_config_site_scope ON theme_config(tenant_id, site_id, config_scope);
