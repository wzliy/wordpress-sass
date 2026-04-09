CREATE TABLE IF NOT EXISTS tenant (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    plan_id BIGINT,
    expire_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    site_type VARCHAR(20) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    admin_url VARCHAR(255) NOT NULL,
    auth_type VARCHAR(20) NOT NULL,
    wp_username VARCHAR(100) NOT NULL,
    app_password VARCHAR(255) NOT NULL,
    status TINYINT NOT NULL,
    provision_status VARCHAR(20) NOT NULL,
    status_msg TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS post_publish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    publish_status VARCHAR(20) NOT NULL,
    target_status VARCHAR(20) NOT NULL,
    last_http_status INT,
    remote_post_id BIGINT,
    remote_post_url VARCHAR(255),
    error_message TEXT,
    response_body TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    max_retry_count INT NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP NULL,
    started_at TIMESTAMP NULL,
    finished_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_tenant_id ON user(tenant_id);
CREATE INDEX IF NOT EXISTS idx_user_tenant_id_id ON user(tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_site_tenant_id ON site(tenant_id);
CREATE INDEX IF NOT EXISTS idx_site_tenant_id_id ON site(tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_post_tenant_id ON post(tenant_id);
CREATE INDEX IF NOT EXISTS idx_post_tenant_id_id ON post(tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_post_publish_tenant_id ON post_publish(tenant_id);
CREATE INDEX IF NOT EXISTS idx_post_publish_tenant_id_id ON post_publish(tenant_id, id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_post_publish_tenant_idempotency ON post_publish(tenant_id, idempotency_key);
