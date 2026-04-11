package com.wpss.wordpresssass.payment.domain;

import java.util.Optional;

public interface PaymentRecordRepository {

    PaymentRecord save(PaymentRecord paymentRecord);

    Optional<PaymentRecord> findByPaymentNo(String paymentNo);

    Optional<PaymentRecord> findLatestByOrderId(Long tenantId, Long orderId);

    void updateCallbackResult(Long tenantId, Long paymentRecordId, PaymentRecordStatus status, String callbackPayload);
}
