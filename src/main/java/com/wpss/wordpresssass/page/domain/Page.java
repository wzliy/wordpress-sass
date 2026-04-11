package com.wpss.wordpresssass.page.domain;

import java.time.LocalDateTime;

public class Page {

    public static final String HOME_PAGE_KEY = "HOME";
    public static final String PRODUCT_PAGE_KEY = "PRODUCT";
    public static final String CHECKOUT_PAGE_KEY = "CHECKOUT";
    public static final String SUCCESS_PAGE_KEY = "SUCCESS";

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final String pageKey;
    private final String pageName;
    private final PageType pageType;
    private final PageStatus status;
    private final Long currentVersionId;
    private final Long publishedVersionId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Page(Long id,
                Long tenantId,
                Long siteId,
                String pageKey,
                String pageName,
                PageType pageType,
                PageStatus status,
                Long currentVersionId,
                Long publishedVersionId,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.pageKey = pageKey;
        this.pageName = pageName;
        this.pageType = pageType;
        this.status = status;
        this.currentVersionId = currentVersionId;
        this.publishedVersionId = publishedVersionId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Page createSystemPage(Long tenantId, Long siteId, String pageKey, String pageName) {
        LocalDateTime now = LocalDateTime.now();
        return new Page(
                null,
                tenantId,
                siteId,
                pageKey,
                pageName,
                PageType.SYSTEM,
                PageStatus.DRAFT_ONLY,
                null,
                null,
                now,
                now
        );
    }

    public static Page createHome(Long tenantId, Long siteId, String pageName) {
        return createSystemPage(tenantId, siteId, HOME_PAGE_KEY, pageName);
    }

    public Page withId(Long newId) {
        return new Page(
                newId,
                tenantId,
                siteId,
                pageKey,
                pageName,
                pageType,
                status,
                currentVersionId,
                publishedVersionId,
                createdAt,
                updatedAt
        );
    }

    public Page withVersionPointers(Long newCurrentVersionId,
                                    Long newPublishedVersionId,
                                    PageStatus newStatus) {
        return new Page(
                id,
                tenantId,
                siteId,
                pageKey,
                pageName,
                pageType,
                newStatus,
                newCurrentVersionId,
                newPublishedVersionId,
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

    public String getPageKey() {
        return pageKey;
    }

    public String getPageName() {
        return pageName;
    }

    public PageType getPageType() {
        return pageType;
    }

    public PageStatus getStatus() {
        return status;
    }

    public Long getCurrentVersionId() {
        return currentVersionId;
    }

    public Long getPublishedVersionId() {
        return publishedVersionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
