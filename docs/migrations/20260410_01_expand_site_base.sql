ALTER TABLE site
    ADD COLUMN site_code VARCHAR(64) NULL AFTER tenant_id,
    ADD COLUMN template_id BIGINT NULL AFTER status_msg,
    ADD COLUMN country_code VARCHAR(10) NULL AFTER template_id,
    ADD COLUMN language_code VARCHAR(10) NULL AFTER country_code,
    ADD COLUMN currency_code VARCHAR(10) NULL AFTER language_code,
    ADD COLUMN theme_color VARCHAR(20) NULL AFTER currency_code,
    ADD COLUMN logo_url VARCHAR(255) NULL AFTER theme_color,
    ADD COLUMN banner_title VARCHAR(255) NULL AFTER logo_url,
    ADD COLUMN banner_subtitle VARCHAR(255) NULL AFTER banner_title;

UPDATE site
SET site_code = CONCAT('site-', id)
WHERE site_code IS NULL OR site_code = '';

UPDATE site
SET theme_color = COALESCE(NULLIF(theme_color, ''), '#2563EB'),
    banner_title = COALESCE(NULLIF(banner_title, ''), name),
    banner_subtitle = COALESCE(NULLIF(banner_subtitle, ''), 'Your storefront is ready to be customized.')
WHERE 1 = 1;

ALTER TABLE site
    MODIFY COLUMN site_code VARCHAR(64) NOT NULL,
    MODIFY COLUMN theme_color VARCHAR(20) NOT NULL,
    MODIFY COLUMN banner_title VARCHAR(255) NOT NULL,
    MODIFY COLUMN banner_subtitle VARCHAR(255) NOT NULL;

CREATE INDEX idx_site_template_id ON site(template_id);
CREATE UNIQUE INDEX uk_site_tenant_code ON site(tenant_id, site_code);
