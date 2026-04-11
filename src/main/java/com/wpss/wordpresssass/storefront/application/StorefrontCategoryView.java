package com.wpss.wordpresssass.storefront.application;

import java.math.BigDecimal;
import java.util.List;

public record StorefrontCategoryView(
        Long siteId,
        String siteName,
        String siteCode,
        String themeColor,
        String currentCategorySlug,
        String currentCategoryName,
        String keyword,
        List<CategoryItem> categories,
        List<ProductCard> products
) {

    public record CategoryItem(
            String slug,
            String name,
            boolean active
    ) {
    }

    public record ProductCard(
            Long id,
            String sku,
            String title,
            String categoryName,
            String coverImage,
            BigDecimal price,
            BigDecimal compareAtPrice
    ) {
    }
}
