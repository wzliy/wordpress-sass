package com.wpss.wordpresssass.storefront.application;

import java.util.List;

public record StorefrontHomeView(
        Long siteId,
        String siteCode,
        String siteName,
        String siteInitial,
        String primaryDomain,
        String themeColor,
        String logoUrl,
        String bannerTitle,
        String bannerSubtitle,
        String languageCode,
        String currencyCode,
        List<MenuItem> menuItems,
        List<String> featuredProductIds
) {

    public record MenuItem(
            String label,
            String path
    ) {
    }
}
