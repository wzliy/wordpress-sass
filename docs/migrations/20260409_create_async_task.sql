CREATE TABLE IF NOT EXISTS async_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    task_type VARCHAR(50) NOT NULL,
    biz_type VARCHAR(30) NOT NULL,
    biz_id BIGINT,
    idempotency_key VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    payload_json TEXT,
    result_json TEXT,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 3,
    next_run_at DATETIME,
    locked_by VARCHAR(100),
    locked_at DATETIME,
    started_at DATETIME,
    finished_at DATETIME,
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_async_task_tenant_status_next_run
    ON async_task(tenant_id, status, next_run_at);

CREATE INDEX idx_async_task_biz
    ON async_task(tenant_id, biz_type, biz_id);

CREATE UNIQUE INDEX uk_async_task_idempotency
    ON async_task(tenant_id, idempotency_key);
