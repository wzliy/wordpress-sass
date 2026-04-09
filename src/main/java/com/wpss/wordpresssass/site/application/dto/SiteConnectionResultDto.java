package com.wpss.wordpresssass.site.application.dto;

public record SiteConnectionResultDto(
        Long siteId,
        boolean success,
        String message
) {
}
