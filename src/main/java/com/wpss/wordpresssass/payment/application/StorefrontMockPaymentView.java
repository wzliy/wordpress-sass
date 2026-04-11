package com.wpss.wordpresssass.payment.application;

import java.math.BigDecimal;

public record StorefrontMockPaymentView(
        String themeColor,
        String siteCode,
        String siteName,
        String paymentNo,
        String orderNo,
        BigDecimal amount,
        String currency,
        String paymentStatus
) {
}
