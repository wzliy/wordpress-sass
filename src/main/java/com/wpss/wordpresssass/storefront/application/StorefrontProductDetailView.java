package com.wpss.wordpresssass.storefront.application;

import java.math.BigDecimal;
import java.util.List;

public record StorefrontProductDetailView(
        Long siteId,
        String siteName,
        String siteCode,
        String themeColor,
        Long productId,
        String sku,
        String title,
        String categoryName,
        String categorySlug,
        String coverImage,
        List<String> galleryImages,
        String descriptionHtml,
        List<String> sizes,
        BigDecimal price,
        BigDecimal compareAtPrice
) {
}
