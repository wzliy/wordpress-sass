package com.wpss.wordpresssass.email.domain;

import java.util.Optional;

public interface EmailRecordRepository {

    EmailRecord save(EmailRecord emailRecord);

    Optional<EmailRecord> findLatestByOrderId(Long tenantId, Long orderId);

    void updateResult(Long tenantId, Long emailRecordId, EmailRecordStatus status, String responseMessage);
}
