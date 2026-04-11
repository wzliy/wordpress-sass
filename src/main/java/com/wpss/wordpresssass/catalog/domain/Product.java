package com.wpss.wordpresssass.catalog.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {

    private final Long id;
    private final Long tenantId;
    private final String sku;
    private final String title;
    private final Long categoryId;
    private final String coverImage;
    private final String galleryJson;
    private final String descriptionHtml;
    private final String sizesJson;
    private final BigDecimal price;
    private final BigDecimal compareAtPrice;
    private final ProductStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Product(Long id,
                   Long tenantId,
                   String sku,
                   String title,
                   Long categoryId,
                   String coverImage,
                   String galleryJson,
                   String descriptionHtml,
                   String sizesJson,
                   BigDecimal price,
                   BigDecimal compareAtPrice,
                   ProductStatus status,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.sku = sku;
        this.title = title;
        this.categoryId = categoryId;
        this.coverImage = coverImage;
        this.galleryJson = galleryJson;
        this.descriptionHtml = descriptionHtml;
        this.sizesJson = sizesJson;
        this.price = price;
        this.compareAtPrice = compareAtPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Product create(Long tenantId,
                                 String sku,
                                 String title,
                                 Long categoryId,
                                 String coverImage,
                                 String galleryJson,
                                 String descriptionHtml,
                                 String sizesJson,
                                 BigDecimal price,
                                 BigDecimal compareAtPrice,
                                 ProductStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return new Product(
                null,
                tenantId,
                sku,
                title,
                categoryId,
                coverImage,
                galleryJson,
                descriptionHtml,
                sizesJson,
                price,
                compareAtPrice,
                status,
                now,
                now
        );
    }

    public Product withId(Long newId) {
        return new Product(
                newId,
                tenantId,
                sku,
                title,
                categoryId,
                coverImage,
                galleryJson,
                descriptionHtml,
                sizesJson,
                price,
                compareAtPrice,
                status,
                createdAt,
                updatedAt
        );
    }

    public Product withUpdated(String newSku,
                               String newTitle,
                               Long newCategoryId,
                               String newCoverImage,
                               String newGalleryJson,
                               String newDescriptionHtml,
                               String newSizesJson,
                               BigDecimal newPrice,
                               BigDecimal newCompareAtPrice,
                               ProductStatus newStatus) {
        return new Product(
                id,
                tenantId,
                newSku,
                newTitle,
                newCategoryId,
                newCoverImage,
                newGalleryJson,
                newDescriptionHtml,
                newSizesJson,
                newPrice,
                newCompareAtPrice,
                newStatus,
                createdAt,
                LocalDateTime.now()
        );
    }

    public Product withStatus(ProductStatus newStatus) {
        return new Product(
                id,
                tenantId,
                sku,
                title,
                categoryId,
                coverImage,
                galleryJson,
                descriptionHtml,
                sizesJson,
                price,
                compareAtPrice,
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

    public String getSku() {
        return sku;
    }

    public String getTitle() {
        return title;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public String getGalleryJson() {
        return galleryJson;
    }

    public String getDescriptionHtml() {
        return descriptionHtml;
    }

    public String getSizesJson() {
        return sizesJson;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
