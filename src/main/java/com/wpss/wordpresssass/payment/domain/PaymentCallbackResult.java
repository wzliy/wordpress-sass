package com.wpss.wordpresssass.payment.domain;

public record PaymentCallbackResult(
        PaymentRecordStatus status,
        String callbackPayload
) {
}
