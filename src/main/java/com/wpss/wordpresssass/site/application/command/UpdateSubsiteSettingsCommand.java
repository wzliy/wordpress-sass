package com.wpss.wordpresssass.site.application.command;

import jakarta.validation.constraints.NotNull;

public record UpdateSubsiteSettingsCommand(
        @NotNull(message = "must not be null")
        Long siteId,
        String siteName,
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
