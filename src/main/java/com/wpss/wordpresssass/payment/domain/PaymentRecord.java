package com.wpss.wordpresssass.payment.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PaymentRecord {

    private final Long id;
    private final Long tenantId;
    private final Long orderId;
    private final String providerCode;
    private final String paymentNo;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentRecordStatus status;
    private final String callbackPayload;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public PaymentRecord(Long id,
                         Long tenantId,
                         Long orderId,
                         String providerCode,
                         String paymentNo,
                         BigDecimal amount,
                         String currency,
                         PaymentRecordStatus status,
                         String callbackPayload,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.providerCode = providerCode;
        this.paymentNo = paymentNo;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.callbackPayload = callbackPayload;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PaymentRecord create(Long tenantId,
                                       Long orderId,
                                       String providerCode,
                                       BigDecimal amount,
                                       String currency) {
        LocalDateTime now = LocalDateTime.now();
        return new PaymentRecord(
                null,
                tenantId,
                orderId,
                providerCode,
                generatePaymentNo(now),
                amount,
                currency,
                PaymentRecordStatus.PENDING,
                null,
                now,
                now
        );
    }

    public PaymentRecord withId(Long newId) {
        return new PaymentRecord(
                newId,
                tenantId,
                orderId,
                providerCode,
                paymentNo,
                amount,
                currency,
                status,
                callbackPayload,
                createdAt,
                updatedAt
        );
    }

    public PaymentRecord withCallbackResult(PaymentRecordStatus newStatus, String newCallbackPayload) {
        return new PaymentRecord(
                id,
                tenantId,
                orderId,
                providerCode,
                paymentNo,
                amount,
                currency,
                newStatus,
                newCallbackPayload,
                createdAt,
                LocalDateTime.now()
        );
    }

    private static String generatePaymentNo(LocalDateTime now) {
        return "PAY" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentRecordStatus getStatus() {
        return status;
    }

    public String getCallbackPayload() {
        return callbackPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
