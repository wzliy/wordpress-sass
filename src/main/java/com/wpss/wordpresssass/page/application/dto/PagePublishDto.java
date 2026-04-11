package com.wpss.wordpresssass.page.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record PagePublishDto(
        Long siteId,
        Long pageId,
        String pageKey,
        Long publishedVersionId,
        String pageStatus,
        String versionStatus,
        JsonNode runtimeConfig,
        LocalDateTime publishedAt
) {
}
