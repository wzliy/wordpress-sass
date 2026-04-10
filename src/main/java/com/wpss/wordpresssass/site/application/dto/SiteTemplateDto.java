package com.wpss.wordpresssass.site.application.dto;

import com.wpss.wordpresssass.site.domain.SiteTemplate;

import java.time.LocalDateTime;

public record SiteTemplateDto(
        Long id,
        String code,
        String name,
        String category,
        String siteType,
        String previewImageUrl,
        String description,
        boolean builtIn,
        LocalDateTime createdAt
) {

    public static SiteTemplateDto from(SiteTemplate template) {
        return new SiteTemplateDto(
                template.getId(),
                template.getCode(),
                template.getName(),
                template.getCategory(),
                template.getSiteType(),
                template.getPreviewImageUrl(),
                template.getDescription(),
                template.isBuiltIn(),
                template.getCreatedAt()
        );
    }
}
