package com.wpss.wordpresssass.site.application.dto;

public record SubsiteSettingsDto(
        Long siteId,
        String siteCode,
        String siteName,
        String siteUrl,
        String supportEmail,
        String supportPhone,
        String whatsapp,
        String facebook,
        String currencyCode,
        String countryCode,
        String languageCode,
        String logisticsText,
        String logoUrl,
        String bannerTitle,
        String bannerSubtitle
) {
}
