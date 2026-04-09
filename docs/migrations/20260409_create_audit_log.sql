CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    operator_user_id BIGINT,
    operator_username VARCHAR(100),
    module_code VARCHAR(30) NOT NULL,
    action_code VARCHAR(30) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    target_name VARCHAR(255),
    request_id VARCHAR(64),
    before_json TEXT,
    after_json TEXT,
    risk_level VARCHAR(20) NOT NULL,
    ip_address VARCHAR(64),
    user_agent VARCHAR(255),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_tenant_module_created
    ON audit_log(tenant_id, module_code, created_at);

CREATE INDEX idx_audit_log_target
    ON audit_log(tenant_id, target_type, target_id, created_at);

CREATE INDEX idx_audit_log_operator
    ON audit_log(tenant_id, operator_user_id, created_at);
