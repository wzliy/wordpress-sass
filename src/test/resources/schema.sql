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
    site_code VARCHAR(64) NOT NULL,
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
    template_id BIGINT,
    country_code VARCHAR(10),
    language_code VARCHAR(10),
    currency_code VARCHAR(10),
    theme_color VARCHAR(20) NOT NULL,
    logo_url VARCHAR(255),
    banner_title VARCHAR(255) NOT NULL,
    banner_subtitle VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(50),
    site_type VARCHAR(30),
    preview_image_url VARCHAR(255),
    description VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    is_builtin TINYINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site_domain (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    domain VARCHAR(255) NOT NULL,
    is_primary TINYINT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    expiry_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site_homepage_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    config_json CLOB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    page_skeleton_json CLOB,
    default_config_json CLOB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS theme_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    config_scope VARCHAR(20) NOT NULL,
    tokens_json CLOB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

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
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS page_layout_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    page_id BIGINT NOT NULL,
    version_no INT NOT NULL,
    version_status VARCHAR(20) NOT NULL,
    schema_version VARCHAR(20) NOT NULL,
    layout_json CLOB,
    compiled_runtime_json CLOB,
    version_note VARCHAR(255),
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP NULL
);

CREATE TABLE IF NOT EXISTS category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    category_id BIGINT NOT NULL,
    cover_image VARCHAR(255),
    gallery_json CLOB,
    description_html CLOB,
    sizes_json CLOB,
    price DECIMAL(10,2) NOT NULL,
    compare_at_price DECIMAL(10,2),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS site_product_publish (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    publish_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    order_no VARCHAR(40) NOT NULL,
    customer_first_name VARCHAR(80) NOT NULL,
    customer_last_name VARCHAR(80) NOT NULL,
    customer_email VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(40),
    country VARCHAR(10) NOT NULL,
    state VARCHAR(80),
    city VARCHAR(80) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    postal_code VARCHAR(40) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    subtotal_amount DECIMAL(10,2) NOT NULL,
    shipping_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    payment_status VARCHAR(20) NOT NULL,
    shipping_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_title VARCHAR(160) NOT NULL,
    size_value VARCHAR(40),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    provider_code VARCHAR(30) NOT NULL,
    payment_no VARCHAR(40) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    callback_payload TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS email_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    template_code VARCHAR(40) NOT NULL,
    recipient VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    response_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS shipment_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    procurement_status VARCHAR(20) NOT NULL,
    shipment_status VARCHAR(20) NOT NULL,
    tracking_no VARCHAR(80),
    carrier VARCHAR(80),
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cloak_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    rule_name VARCHAR(128) NOT NULL,
    priority INT NOT NULL DEFAULT 100,
    status VARCHAR(20) NOT NULL,
    match_mode VARCHAR(20) NOT NULL,
    traffic_percentage INT NOT NULL DEFAULT 100,
    condition_json CLOB NOT NULL,
    result_type VARCHAR(30) NOT NULL,
    result_json CLOB NOT NULL,
    version_no INT NOT NULL DEFAULT 1,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS cloak_hit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    rule_id BIGINT,
    decision VARCHAR(30) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    request_summary_json CLOB NOT NULL,
    matched_condition_json CLOB,
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
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_tenant_code ON site(tenant_id, site_code);
CREATE INDEX IF NOT EXISTS idx_site_template_id ON site(template_id);
CREATE INDEX IF NOT EXISTS idx_site_template_tenant_status ON site_template(tenant_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_template_tenant_code ON site_template(tenant_id, code);
CREATE INDEX IF NOT EXISTS idx_site_domain_tenant_site ON site_domain(tenant_id, site_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_domain_domain ON site_domain(domain);
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_homepage_config_site ON site_homepage_config(tenant_id, site_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_setting_site ON site_setting(tenant_id, site_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_theme_config_site_scope ON theme_config(tenant_id, site_id, config_scope);
CREATE UNIQUE INDEX IF NOT EXISTS uk_page_tenant_site_key ON page(tenant_id, site_id, page_key);
CREATE INDEX IF NOT EXISTS idx_page_tenant_site_status ON page(tenant_id, site_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_page_layout_version_tenant_page_no ON page_layout_version(tenant_id, page_id, version_no);
CREATE INDEX IF NOT EXISTS idx_page_layout_version_tenant_site_page ON page_layout_version(tenant_id, site_id, page_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_category_tenant_slug ON category(tenant_id, slug);
CREATE INDEX IF NOT EXISTS idx_category_tenant_status ON category(tenant_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_product_tenant_sku ON product(tenant_id, sku);
CREATE INDEX IF NOT EXISTS idx_product_tenant_category_status ON product(tenant_id, category_id, status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_site_product_publish_tenant_site_product ON site_product_publish(tenant_id, site_id, product_id);
CREATE INDEX IF NOT EXISTS idx_site_product_publish_tenant_product_status ON site_product_publish(tenant_id, product_id, publish_status);
CREATE INDEX IF NOT EXISTS idx_site_product_publish_tenant_site_status ON site_product_publish(tenant_id, site_id, publish_status);
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_order_no ON orders(order_no);
CREATE INDEX IF NOT EXISTS idx_orders_tenant_site_created ON orders(tenant_id, site_id, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_tenant_status ON orders(tenant_id, order_status, payment_status, shipping_status);
CREATE INDEX IF NOT EXISTS idx_order_item_tenant_order ON order_item(tenant_id, order_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_record_payment_no ON payment_record(payment_no);
CREATE INDEX IF NOT EXISTS idx_payment_record_tenant_order ON payment_record(tenant_id, order_id, created_at);
CREATE INDEX IF NOT EXISTS idx_payment_record_tenant_status ON payment_record(tenant_id, status, updated_at);
CREATE INDEX IF NOT EXISTS idx_email_record_tenant_order ON email_record(tenant_id, order_id, created_at);
CREATE INDEX IF NOT EXISTS idx_email_record_tenant_status ON email_record(tenant_id, status, updated_at);
CREATE UNIQUE INDEX IF NOT EXISTS uk_shipment_record_tenant_order ON shipment_record(tenant_id, order_id);
CREATE INDEX IF NOT EXISTS idx_shipment_record_tenant_tracking ON shipment_record(tenant_id, tracking_no);
CREATE INDEX IF NOT EXISTS idx_shipment_record_tenant_status ON shipment_record(tenant_id, procurement_status, shipment_status, updated_at);
CREATE UNIQUE INDEX IF NOT EXISTS uk_cloak_rule_tenant_site_name ON cloak_rule(tenant_id, site_id, rule_name);
CREATE INDEX IF NOT EXISTS idx_cloak_rule_tenant_site_status_priority ON cloak_rule(tenant_id, site_id, status, priority);
CREATE INDEX IF NOT EXISTS idx_cloak_hit_log_tenant_site_created ON cloak_hit_log(tenant_id, site_id, created_at);
CREATE INDEX IF NOT EXISTS idx_cloak_hit_log_tenant_rule_created ON cloak_hit_log(tenant_id, rule_id, created_at);

CREATE INDEX IF NOT EXISTS idx_post_tenant_id ON post(tenant_id);
CREATE INDEX IF NOT EXISTS idx_post_tenant_id_id ON post(tenant_id, id);

CREATE INDEX IF NOT EXISTS idx_post_publish_tenant_id ON post_publish(tenant_id);
CREATE INDEX IF NOT EXISTS idx_post_publish_tenant_id_id ON post_publish(tenant_id, id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_post_publish_tenant_idempotency ON post_publish(tenant_id, idempotency_key);
