CREATE TABLE email_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    template_code VARCHAR(40) NOT NULL,
    recipient VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_message TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_record_tenant_order ON email_record(tenant_id, order_id, created_at);
CREATE INDEX idx_email_record_tenant_status ON email_record(tenant_id, status, updated_at);
