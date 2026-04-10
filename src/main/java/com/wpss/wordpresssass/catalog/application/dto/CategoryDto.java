package com.wpss.wordpresssass.catalog.application.dto;

import com.wpss.wordpresssass.catalog.domain.Category;

import java.time.LocalDateTime;

public record CategoryDto(
        Long id,
        Long tenantId,
        String name,
        String slug,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CategoryDto from(Category category) {
        return new CategoryDto(
                category.getId(),
                category.getTenantId(),
                category.getName(),
                category.getSlug(),
                category.getStatus().name(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
