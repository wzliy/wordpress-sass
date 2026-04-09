ALTER TABLE user
    MODIFY COLUMN tenant_id BIGINT NOT NULL;

ALTER TABLE site
    MODIFY COLUMN tenant_id BIGINT NOT NULL;

ALTER TABLE post
    MODIFY COLUMN tenant_id BIGINT NOT NULL;

ALTER TABLE post_publish
    MODIFY COLUMN tenant_id BIGINT NOT NULL;

ALTER TABLE task
    MODIFY COLUMN tenant_id BIGINT NOT NULL;

CREATE INDEX idx_user_tenant_id ON user(tenant_id);
CREATE INDEX idx_user_tenant_id_id ON user(tenant_id, id);

CREATE INDEX idx_site_tenant_id ON site(tenant_id);
CREATE INDEX idx_site_tenant_id_id ON site(tenant_id, id);

CREATE INDEX idx_post_tenant_id ON post(tenant_id);
CREATE INDEX idx_post_tenant_id_id ON post(tenant_id, id);

CREATE INDEX idx_post_publish_tenant_id ON post_publish(tenant_id);
CREATE INDEX idx_post_publish_tenant_id_id ON post_publish(tenant_id, id);
CREATE UNIQUE INDEX uk_post_publish_tenant_idempotency ON post_publish(tenant_id, idempotency_key);

CREATE INDEX idx_task_tenant_id ON task(tenant_id);
