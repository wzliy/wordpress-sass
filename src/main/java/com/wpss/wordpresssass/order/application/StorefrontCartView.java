package com.wpss.wordpresssass.order.application;

import java.math.BigDecimal;
import java.util.List;

public record StorefrontCartView(
        Long siteId,
        String siteName,
        String siteCode,
        String themeColor,
        List<LineItem> items,
        int totalQuantity,
        BigDecimal subtotal
) {

    public record LineItem(
            Long productId,
            String sku,
            String title,
            String coverImage,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal
    ) {
    }
}
