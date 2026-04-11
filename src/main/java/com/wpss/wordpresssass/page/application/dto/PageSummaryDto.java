package com.wpss.wordpresssass.page.application.dto;

import com.wpss.wordpresssass.page.domain.Page;

import java.time.LocalDateTime;

public record PageSummaryDto(
        Long pageId,
        Long siteId,
        String pageKey,
        String pageName,
        String pageType,
        String status,
        Long currentVersionId,
        Long publishedVersionId,
        LocalDateTime updatedAt
) {

    public static PageSummaryDto from(Page page) {
        return new PageSummaryDto(
                page.getId(),
                page.getSiteId(),
                page.getPageKey(),
                page.getPageName(),
                page.getPageType().name(),
                page.getStatus().name(),
                page.getCurrentVersionId(),
                page.getPublishedVersionId(),
                page.getUpdatedAt()
        );
    }
}
