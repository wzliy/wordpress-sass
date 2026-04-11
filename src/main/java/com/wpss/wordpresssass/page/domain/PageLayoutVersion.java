package com.wpss.wordpresssass.page.domain;

import java.time.LocalDateTime;

public class PageLayoutVersion {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final Long pageId;
    private final Integer versionNo;
    private final PageVersionStatus versionStatus;
    private final String schemaVersion;
    private final String layoutJson;
    private final String compiledRuntimeJson;
    private final String versionNote;
    private final String createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime publishedAt;

    public PageLayoutVersion(Long id,
                             Long tenantId,
                             Long siteId,
                             Long pageId,
                             Integer versionNo,
                             PageVersionStatus versionStatus,
                             String schemaVersion,
                             String layoutJson,
                             String compiledRuntimeJson,
                             String versionNote,
                             String createdBy,
                             LocalDateTime createdAt,
                             LocalDateTime publishedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.pageId = pageId;
        this.versionNo = versionNo;
        this.versionStatus = versionStatus;
        this.schemaVersion = schemaVersion;
        this.layoutJson = layoutJson;
        this.compiledRuntimeJson = compiledRuntimeJson;
        this.versionNote = versionNote;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.publishedAt = publishedAt;
    }

    public static PageLayoutVersion initialPublished(Long tenantId,
                                                     Long siteId,
                                                     Long pageId,
                                                     String layoutJson,
                                                     String compiledRuntimeJson,
                                                     String versionNote) {
        LocalDateTime now = LocalDateTime.now();
        return new PageLayoutVersion(
                null,
                tenantId,
                siteId,
                pageId,
                1,
                PageVersionStatus.PUBLISHED,
                "v1",
                layoutJson,
                compiledRuntimeJson,
                versionNote,
                "system",
                now,
                now
        );
    }

    public static PageLayoutVersion draft(Long tenantId,
                                          Long siteId,
                                          Long pageId,
                                          Integer versionNo,
                                          String layoutJson,
                                          String versionNote,
                                          String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return new PageLayoutVersion(
                null,
                tenantId,
                siteId,
                pageId,
                versionNo,
                PageVersionStatus.DRAFT,
                "v1",
                layoutJson,
                null,
                versionNote,
                createdBy,
                now,
                null
        );
    }

    public static PageLayoutVersion rollbackDraft(Long tenantId,
                                                  Long siteId,
                                                  Long pageId,
                                                  Integer versionNo,
                                                  String layoutJson,
                                                  String versionNote,
                                                  String createdBy) {
        return draft(tenantId, siteId, pageId, versionNo, layoutJson, versionNote, createdBy);
    }

    public PageLayoutVersion withId(Long newId) {
        return new PageLayoutVersion(
                newId,
                tenantId,
                siteId,
                pageId,
                versionNo,
                versionStatus,
                schemaVersion,
                layoutJson,
                compiledRuntimeJson,
                versionNote,
                createdBy,
                createdAt,
                publishedAt
        );
    }

    public PageLayoutVersion withUpdatedDraft(String newLayoutJson, String newVersionNote) {
        return new PageLayoutVersion(
                id,
                tenantId,
                siteId,
                pageId,
                versionNo,
                versionStatus,
                schemaVersion,
                newLayoutJson,
                compiledRuntimeJson,
                newVersionNote,
                createdBy,
                createdAt,
                publishedAt
        );
    }

    public PageLayoutVersion asPublished(String newCompiledRuntimeJson, String newVersionNote) {
        return new PageLayoutVersion(
                id,
                tenantId,
                siteId,
                pageId,
                versionNo,
                PageVersionStatus.PUBLISHED,
                schemaVersion,
                layoutJson,
                newCompiledRuntimeJson,
                newVersionNote,
                createdBy,
                createdAt,
                LocalDateTime.now()
        );
    }

    public PageLayoutVersion asArchived() {
        return new PageLayoutVersion(
                id,
                tenantId,
                siteId,
                pageId,
                versionNo,
                PageVersionStatus.ARCHIVED,
                schemaVersion,
                layoutJson,
                compiledRuntimeJson,
                versionNote,
                createdBy,
                createdAt,
                publishedAt
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

    public Long getPageId() {
        return pageId;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public PageVersionStatus getVersionStatus() {
        return versionStatus;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public String getLayoutJson() {
        return layoutJson;
    }

    public String getCompiledRuntimeJson() {
        return compiledRuntimeJson;
    }

    public String getVersionNote() {
        return versionNote;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
