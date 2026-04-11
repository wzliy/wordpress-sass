package com.wpss.wordpresssass.catalog.application.dto;

import java.time.LocalDateTime;

public record SiteProductPublishDto(
        Long id,
        Long siteId,
        String siteCode,
        String siteName,
        String siteDomain,
        Integer siteStatus,
        Long productId,
        String productSku,
        String productTitle,
        String publishStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
