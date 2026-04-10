package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;

public class SiteTemplate {

    private final Long id;
    private final Long tenantId;
    private final String code;
    private final String name;
    private final String category;
    private final String siteType;
    private final String previewImageUrl;
    private final String description;
    private final String status;
    private final boolean builtIn;
    private final LocalDateTime createdAt;

    public SiteTemplate(Long id,
                        Long tenantId,
                        String code,
                        String name,
                        String category,
                        String siteType,
                        String previewImageUrl,
                        String description,
                        String status,
                        boolean builtIn,
                        LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.category = category;
        this.siteType = siteType;
        this.previewImageUrl = previewImageUrl;
        this.description = description;
        this.status = status;
        this.builtIn = builtIn;
        this.createdAt = createdAt;
    }

    public static SiteTemplate builtIn(String code,
                                       String name,
                                       String category,
                                       String siteType,
                                       String previewImageUrl,
                                       String description) {
        return new SiteTemplate(
                null,
                0L,
                code,
                name,
                category,
                siteType,
                previewImageUrl,
                description,
                "ACTIVE",
                true,
                LocalDateTime.now()
        );
    }

    public SiteTemplate withId(Long newId) {
        return new SiteTemplate(newId, tenantId, code, name, category, siteType, previewImageUrl, description, status, builtIn, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getSiteType() {
        return siteType;
    }

    public String getPreviewImageUrl() {
        return previewImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
