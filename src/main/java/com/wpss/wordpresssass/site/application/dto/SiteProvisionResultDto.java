package com.wpss.wordpresssass.site.application.dto;

public record SiteProvisionResultDto(
        Long siteId,
        String domain,
        String adminUrl,
        String wpUsername,
        String appPassword,
        String provisionStatus,
        String message
) {
}
