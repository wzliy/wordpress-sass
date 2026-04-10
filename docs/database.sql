-- Tenant
DROP TABLE IF EXISTS tenant;
CREATE TABLE IF NOT EXISTS tenant (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100),
                        plan_id BIGINT,
                        expire_time DATETIME,
                        created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- User
DROP TABLE IF EXISTS user;
CREATE TABLE IF NOT EXISTS user (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      username VARCHAR(50),
                      password VARCHAR(255),
                      email VARCHAR(100),
                      nickname VARCHAR(50),
                      role VARCHAR(20),
                      status VARCHAR(20) DEFAULT 'ACTIVE',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Plan
DROP TABLE IF EXISTS plan;
CREATE TABLE IF NOT EXISTS plan (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50),
                      max_sites INT,
                      price DECIMAL(10,2),
                      duration_days INT
);

-- Site (WordPress)
DROP TABLE IF EXISTS site;
CREATE TABLE IF NOT EXISTS site (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      site_code VARCHAR(64) NOT NULL,
                      name VARCHAR(100),
                      site_type VARCHAR(20),
                      base_url VARCHAR(255),
                      domain VARCHAR(255),
                      admin_url VARCHAR(255),

                      auth_type VARCHAR(20),
                      wp_username VARCHAR(100),
                      app_password VARCHAR(255),

                      status TINYINT DEFAULT 1,
                      provision_status VARCHAR(20),
                      status_msg TEXT,
                      template_id BIGINT,
                      country_code VARCHAR(10),
                      language_code VARCHAR(10),
                      currency_code VARCHAR(10),
                      theme_color VARCHAR(20) NOT NULL DEFAULT '#2563EB',
                      logo_url VARCHAR(255),
                      banner_title VARCHAR(255) NOT NULL,
                      banner_subtitle VARCHAR(255) NOT NULL,

                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Site Template
DROP TABLE IF EXISTS site_template;
CREATE TABLE IF NOT EXISTS site_template (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      code VARCHAR(64) NOT NULL,
                      name VARCHAR(120) NOT NULL,
                      category VARCHAR(50),
                      site_type VARCHAR(30),
                      preview_image_url VARCHAR(255),
                      description VARCHAR(500),
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      is_builtin TINYINT NOT NULL DEFAULT 0,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Site Domain
DROP TABLE IF EXISTS site_domain;
CREATE TABLE IF NOT EXISTS site_domain (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      site_id BIGINT NOT NULL,
                      domain VARCHAR(255) NOT NULL,
                      is_primary TINYINT NOT NULL DEFAULT 0,
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      expiry_at DATETIME,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Site Homepage Config
DROP TABLE IF EXISTS site_homepage_config;
CREATE TABLE IF NOT EXISTS site_homepage_config (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      site_id BIGINT NOT NULL,
                      config_json TEXT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Site Setting
DROP TABLE IF EXISTS site_setting;
CREATE TABLE IF NOT EXISTS site_setting (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      site_id BIGINT NOT NULL,
                      page_skeleton_json TEXT,
                      default_config_json TEXT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Theme Config
DROP TABLE IF EXISTS theme_config;
CREATE TABLE IF NOT EXISTS theme_config (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      site_id BIGINT NOT NULL,
                      config_scope VARCHAR(20) NOT NULL,
                      tokens_json TEXT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Category
DROP TABLE IF EXISTS category;
CREATE TABLE IF NOT EXISTS category (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      name VARCHAR(120) NOT NULL,
                      slug VARCHAR(120) NOT NULL,
                      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Post
DROP TABLE IF EXISTS post;
CREATE TABLE IF NOT EXISTS post (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      title VARCHAR(255),
                      content TEXT,
                      status VARCHAR(20),
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Publish Record (IMPORTANT)
DROP TABLE IF EXISTS post_publish;
CREATE TABLE IF NOT EXISTS post_publish (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              tenant_id BIGINT NOT NULL,
                              post_id BIGINT,
                              site_id BIGINT,
                              idempotency_key VARCHAR(64),
                              publish_status VARCHAR(20),
                              target_status VARCHAR(20) DEFAULT 'publish',
                              last_http_status INT,
                              remote_post_id BIGINT,
                              remote_post_url VARCHAR(255),
                              error_message TEXT,
                              response_body TEXT,
                              retry_count INT DEFAULT 0,
                              max_retry_count INT DEFAULT 3,
                              next_retry_at DATETIME,
                              started_at DATETIME,
                              finished_at DATETIME,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Task (schedule)
DROP TABLE IF EXISTS task;
CREATE TABLE IF NOT EXISTS task (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      post_id BIGINT,
                      cron_expr VARCHAR(50),
                      next_run_time DATETIME
);

-- Unified Async Task
DROP TABLE IF EXISTS async_task;
CREATE TABLE IF NOT EXISTS async_task (
                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                      tenant_id BIGINT NOT NULL,
                      task_type VARCHAR(50) NOT NULL,
                      biz_type VARCHAR(30) NOT NULL,
                      biz_id BIGINT,
                      idempotency_key VARCHAR(100) NOT NULL,
                      status VARCHAR(20) NOT NULL,
                      priority INT DEFAULT 100,
                      payload_json TEXT,
                      result_json TEXT,
                      error_message TEXT,
                      retry_count INT DEFAULT 0,
                      max_retry_count INT DEFAULT 3,
                      next_run_at DATETIME,
                      locked_by VARCHAR(100),
                      locked_at DATETIME,
                      started_at DATETIME,
                      finished_at DATETIME,
                      created_by BIGINT,
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log
DROP TABLE IF EXISTS audit_log;
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
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_tenant_id ON user(tenant_id);
CREATE INDEX idx_user_tenant_id_id ON user(tenant_id, id);

CREATE INDEX idx_site_tenant_id ON site(tenant_id);
CREATE INDEX idx_site_tenant_id_id ON site(tenant_id, id);
CREATE UNIQUE INDEX uk_site_tenant_code ON site(tenant_id, site_code);
CREATE INDEX idx_site_template_id ON site(template_id);
CREATE INDEX idx_site_template_tenant_status ON site_template(tenant_id, status);
CREATE UNIQUE INDEX uk_site_template_tenant_code ON site_template(tenant_id, code);
CREATE INDEX idx_site_domain_tenant_site ON site_domain(tenant_id, site_id);
CREATE UNIQUE INDEX uk_site_domain_domain ON site_domain(domain);
CREATE UNIQUE INDEX uk_site_homepage_config_site ON site_homepage_config(tenant_id, site_id);
CREATE UNIQUE INDEX uk_site_setting_site ON site_setting(tenant_id, site_id);
CREATE UNIQUE INDEX uk_theme_config_site_scope ON theme_config(tenant_id, site_id, config_scope);
CREATE UNIQUE INDEX uk_category_tenant_slug ON category(tenant_id, slug);
CREATE INDEX idx_category_tenant_status ON category(tenant_id, status);

CREATE INDEX idx_post_tenant_id ON post(tenant_id);
CREATE INDEX idx_post_tenant_id_id ON post(tenant_id, id);

CREATE INDEX idx_post_publish_tenant_id ON post_publish(tenant_id);
CREATE INDEX idx_post_publish_tenant_id_id ON post_publish(tenant_id, id);
CREATE UNIQUE INDEX uk_post_publish_tenant_idempotency ON post_publish(tenant_id, idempotency_key);

CREATE INDEX idx_task_tenant_id ON task(tenant_id);
CREATE INDEX idx_async_task_tenant_status_next_run ON async_task(tenant_id, status, next_run_at);
CREATE INDEX idx_async_task_biz ON async_task(tenant_id, biz_type, biz_id);
CREATE UNIQUE INDEX uk_async_task_idempotency ON async_task(tenant_id, idempotency_key);
CREATE INDEX idx_audit_log_tenant_module_created ON audit_log(tenant_id, module_code, created_at);
CREATE INDEX idx_audit_log_target ON audit_log(tenant_id, target_type, target_id, created_at);
CREATE INDEX idx_audit_log_operator ON audit_log(tenant_id, operator_user_id, created_at);
