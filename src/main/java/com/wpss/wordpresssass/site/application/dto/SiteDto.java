package com.wpss.wordpresssass.site.application.dto;

import com.wpss.wordpresssass.site.domain.Site;

import java.time.LocalDateTime;

public record SiteDto(
        Long id,
        Long tenantId,
        String siteCode,
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
        Long templateId,
        String countryCode,
        String languageCode,
        String currencyCode,
        String themeColor,
        String logoUrl,
        String bannerTitle,
        String bannerSubtitle,
        LocalDateTime createdAt
) {

    public static SiteDto from(Site site) {
        return new SiteDto(
                site.getId(),
                site.getTenantId(),
                site.getSiteCode(),
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
                site.getTemplateId(),
                site.getCountryCode(),
                site.getLanguageCode(),
                site.getCurrencyCode(),
                site.getThemeColor(),
                site.getLogoUrl(),
                site.getBannerTitle(),
                site.getBannerSubtitle(),
                site.getCreatedAt()
        );
    }
}
