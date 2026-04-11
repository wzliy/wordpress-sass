CREATE TABLE IF NOT EXISTS cloak_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    status VARCHAR(20) NOT NULL,
    match_mode VARCHAR(20) NOT NULL,
    traffic_percentage INT NOT NULL DEFAULT 100,
    condition_json TEXT NOT NULL,
    result_type VARCHAR(30) NOT NULL,
    result_json TEXT NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    created_by VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cloak_hit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    rule_id BIGINT,
    decision VARCHAR(30) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    request_summary_json TEXT NOT NULL,
    matched_condition_json TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_cloak_rule_tenant_site_name ON cloak_rule(tenant_id, site_id, rule_name);
CREATE INDEX idx_cloak_rule_tenant_site_status_priority ON cloak_rule(tenant_id, site_id, status, priority);
CREATE INDEX idx_cloak_hit_log_tenant_site_created ON cloak_hit_log(tenant_id, site_id, created_at);
CREATE INDEX idx_cloak_hit_log_tenant_rule_created ON cloak_hit_log(tenant_id, rule_id, created_at);
