package com.wpss.wordpresssass.site.infrastructure.dataobject;

import java.time.LocalDateTime;

public class ThemeConfigDO {

    private Long tenantId;
    private Long siteId;
    private String configScope;
    private String tokensJson;
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

    public String getConfigScope() {
        return configScope;
    }

    public void setConfigScope(String configScope) {
        this.configScope = configScope;
    }

    public String getTokensJson() {
        return tokensJson;
    }

    public void setTokensJson(String tokensJson) {
        this.tokensJson = tokensJson;
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
