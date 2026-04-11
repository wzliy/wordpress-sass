package com.wpss.wordpresssass.email.domain;

import java.time.LocalDateTime;

public class EmailRecord {

    private final Long id;
    private final Long tenantId;
    private final Long orderId;
    private final String templateCode;
    private final String recipient;
    private final EmailRecordStatus status;
    private final String responseMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public EmailRecord(Long id,
                       Long tenantId,
                       Long orderId,
                       String templateCode,
                       String recipient,
                       EmailRecordStatus status,
                       String responseMessage,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.templateCode = templateCode;
        this.recipient = recipient;
        this.status = status;
        this.responseMessage = responseMessage;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EmailRecord create(Long tenantId,
                                     Long orderId,
                                     String templateCode,
                                     String recipient) {
        LocalDateTime now = LocalDateTime.now();
        return new EmailRecord(
                null,
                tenantId,
                orderId,
                templateCode,
                recipient,
                EmailRecordStatus.PENDING,
                null,
                now,
                now
        );
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

    public String getTemplateCode() {
        return templateCode;
    }

    public String getRecipient() {
        return recipient;
    }

    public EmailRecordStatus getStatus() {
        return status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
