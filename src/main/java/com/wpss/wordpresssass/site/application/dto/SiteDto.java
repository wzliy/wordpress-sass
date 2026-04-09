package com.wpss.wordpresssass.site.application.dto;

import com.wpss.wordpresssass.site.domain.Site;

import java.time.LocalDateTime;

public record SiteDto(
        Long id,
        Long tenantId,
        String name,
        String siteType,
        String baseUrl,
        String domain,
        String adminUrl,
        String authType,
        String wpUsername,
        Integer status,
        String provisionStatus,
        String statusMessage,
        LocalDateTime createdAt
) {

    public static SiteDto from(Site site) {
        return new SiteDto(
                site.getId(),
                site.getTenantId(),
                site.getName(),
                site.getSiteType().name(),
                site.getBaseUrl(),
                site.getDomain(),
                site.getAdminUrl(),
                site.getAuthType(),
                site.getWpUsername(),
                site.getStatus().getCode(),
                site.getProvisionStatus().name(),
                site.getStatusMessage(),
                site.getCreatedAt()
        );
    }
}
