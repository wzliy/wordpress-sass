package com.wpss.wordpresssass.page.application.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

public record PageEditorDto(
        Long siteId,
        Long pageId,
        String pageKey,
        String pageName,
        String pageType,
        String pageStatus,
        Long currentVersionId,
        Long publishedVersionId,
        Integer currentVersionNo,
        String currentVersionStatus,
        LocalDateTime currentVersionCreatedAt,
        JsonNode layout,
        List<BlockSchemaDto> blockLibrary
) {

    public record BlockSchemaDto(
            String type,
            String label,
            String category,
            List<FieldSchemaDto> fields
    ) {
    }

    public record FieldSchemaDto(
            String name,
            String label,
            String inputType,
            boolean required
    ) {
    }
}
