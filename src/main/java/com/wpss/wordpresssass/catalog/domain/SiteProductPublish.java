package com.wpss.wordpresssass.catalog.domain;

import java.time.LocalDateTime;

public class SiteProductPublish {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final Long productId;
    private final SiteProductPublishStatus publishStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public SiteProductPublish(Long id,
                              Long tenantId,
                              Long siteId,
                              Long productId,
                              SiteProductPublishStatus publishStatus,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.productId = productId;
        this.publishStatus = publishStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SiteProductPublish create(Long tenantId,
                                            Long siteId,
                                            Long productId,
                                            SiteProductPublishStatus publishStatus) {
        LocalDateTime now = LocalDateTime.now();
        return new SiteProductPublish(
                null,
                tenantId,
                siteId,
                productId,
                publishStatus,
                now,
                now
        );
    }

    public SiteProductPublish withId(Long newId) {
        return new SiteProductPublish(
                newId,
                tenantId,
                siteId,
                productId,
                publishStatus,
                createdAt,
                updatedAt
        );
    }

    public SiteProductPublish withStatus(SiteProductPublishStatus newStatus) {
        return new SiteProductPublish(
                id,
                tenantId,
                siteId,
                productId,
                newStatus,
                createdAt,
                LocalDateTime.now()
        );
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

    public Long getProductId() {
        return productId;
    }

    public SiteProductPublishStatus getPublishStatus() {
        return publishStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
