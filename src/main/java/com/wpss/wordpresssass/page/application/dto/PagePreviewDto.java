package com.wpss.wordpresssass.page.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record PagePreviewDto(
        Long siteId,
        Long pageId,
        String pageKey,
        Long versionId,
        String versionStatus,
        JsonNode runtimeConfig,
        LocalDateTime generatedAt
) {
}
