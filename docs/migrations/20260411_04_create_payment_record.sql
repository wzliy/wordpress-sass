CREATE TABLE payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    provider_code VARCHAR(30) NOT NULL,
    payment_no VARCHAR(40) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    callback_payload TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_payment_record_payment_no ON payment_record(payment_no);
CREATE INDEX idx_payment_record_tenant_order ON payment_record(tenant_id, order_id, created_at);
CREATE INDEX idx_payment_record_tenant_status ON payment_record(tenant_id, status, updated_at);
