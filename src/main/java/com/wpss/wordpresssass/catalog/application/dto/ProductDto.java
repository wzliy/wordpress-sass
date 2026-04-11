package com.wpss.wordpresssass.catalog.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductDto(
        Long id,
        Long tenantId,
        String sku,
        String title,
        Long categoryId,
        String categoryName,
        String coverImage,
        List<String> galleryImages,
        String descriptionHtml,
        List<String> sizes,
        BigDecimal price,
        BigDecimal compareAtPrice,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
