package com.wpss.wordpresssass.order.application;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record StorefrontOrderSuccessView(
        Long siteId,
        String siteName,
        String siteCode,
        String themeColor,
        String orderNo,
        String customerName,
        String customerEmail,
        BigDecimal totalAmount,
        String currency,
        String orderStatus,
        String paymentStatus,
        LocalDateTime createdAt,
        List<LineItem> items
) {

    public record LineItem(
            String productTitle,
            Integer quantity,
            BigDecimal lineTotal
    ) {
    }
}
