package com.wpss.wordpresssass.catalog.domain;

import java.time.LocalDateTime;

public class Category {

    private final Long id;
    private final Long tenantId;
    private final String name;
    private final String slug;
    private final CategoryStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Category(Long id,
                    Long tenantId,
                    String name,
                    String slug,
                    CategoryStatus status,
                    LocalDateTime createdAt,
                    LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.slug = slug;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Category create(Long tenantId, String name, String slug) {
        LocalDateTime now = LocalDateTime.now();
        return new Category(
                null,
                tenantId,
                name,
                slug,
                CategoryStatus.ACTIVE,
                now,
                now
        );
    }

    public Category withId(Long newId) {
        return new Category(newId, tenantId, name, slug, status, createdAt, updatedAt);
    }

    public Category withStatus(CategoryStatus newStatus) {
        return new Category(id, tenantId, name, slug, newStatus, createdAt, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
