CREATE TABLE shipment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    procurement_status VARCHAR(20) NOT NULL,
    shipment_status VARCHAR(20) NOT NULL,
    tracking_no VARCHAR(80),
    carrier VARCHAR(80),
    failure_reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_shipment_record_tenant_order ON shipment_record(tenant_id, order_id);
CREATE INDEX idx_shipment_record_tenant_tracking ON shipment_record(tenant_id, tracking_no);
CREATE INDEX idx_shipment_record_tenant_status ON shipment_record(tenant_id, procurement_status, shipment_status, updated_at);
