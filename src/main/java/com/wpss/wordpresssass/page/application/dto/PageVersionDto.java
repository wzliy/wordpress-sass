package com.wpss.wordpresssass.page.application.dto;

import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageLayoutVersion;

import java.time.LocalDateTime;

public record PageVersionDto(
        Long versionId,
        Integer versionNo,
        String versionStatus,
        String versionNote,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime publishedAt,
        boolean currentVersion,
        boolean publishedVersion
) {

    public static PageVersionDto from(Page page, PageLayoutVersion version) {
        return new PageVersionDto(
                version.getId(),
                version.getVersionNo(),
                version.getVersionStatus().name(),
                version.getVersionNote(),
                version.getCreatedBy(),
                version.getCreatedAt(),
                version.getPublishedAt(),
                version.getId().equals(page.getCurrentVersionId()),
                version.getId().equals(page.getPublishedVersionId())
        );
    }
}
