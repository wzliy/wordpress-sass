package com.wpss.wordpresssass.site.infrastructure.dataobject;

import java.time.LocalDateTime;

public class SiteSettingDO {

    private Long tenantId;
    private Long siteId;
    private String pageSkeletonJson;
    private String defaultConfigJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public String getPageSkeletonJson() {
        return pageSkeletonJson;
    }

    public void setPageSkeletonJson(String pageSkeletonJson) {
        this.pageSkeletonJson = pageSkeletonJson;
    }

    public String getDefaultConfigJson() {
        return defaultConfigJson;
    }

    public void setDefaultConfigJson(String defaultConfigJson) {
        this.defaultConfigJson = defaultConfigJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
