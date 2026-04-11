package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;

public class SiteSetting {

    private final Long tenantId;
    private final Long siteId;
    private final String pageSkeletonJson;
    private final String defaultConfigJson;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SiteSetting(Long tenantId,
                       Long siteId,
                       String pageSkeletonJson,
                       String defaultConfigJson,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.pageSkeletonJson = pageSkeletonJson;
        this.defaultConfigJson = defaultConfigJson;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getPageSkeletonJson() {
        return pageSkeletonJson;
    }

    public String getDefaultConfigJson() {
        return defaultConfigJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
