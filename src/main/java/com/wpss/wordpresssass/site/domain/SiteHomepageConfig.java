package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;

public class SiteHomepageConfig {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final String configJson;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SiteHomepageConfig(Long id,
                              Long tenantId,
                              Long siteId,
                              String configJson,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.configJson = configJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getConfigJson() {
        return configJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
