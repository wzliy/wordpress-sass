package com.wpss.wordpresssass.email.infrastructure;

import com.wpss.wordpresssass.email.domain.EmailRecord;
import com.wpss.wordpresssass.email.domain.EmailRecordRepository;
import com.wpss.wordpresssass.email.domain.EmailRecordStatus;
import com.wpss.wordpresssass.email.infrastructure.dataobject.EmailRecordDO;
import com.wpss.wordpresssass.email.infrastructure.mapper.EmailRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisEmailRecordRepository implements EmailRecordRepository {

    private final EmailRecordMapper emailRecordMapper;

    public MybatisEmailRecordRepository(EmailRecordMapper emailRecordMapper) {
        this.emailRecordMapper = emailRecordMapper;
    }

    @Override
    public EmailRecord save(EmailRecord emailRecord) {
        EmailRecordDO emailRecordDO = toDataObject(emailRecord);
        emailRecordMapper.insert(emailRecordDO);
        return toDomain(emailRecordDO);
    }

    @Override
    public Optional<EmailRecord> findLatestByOrderId(Long tenantId, Long orderId) {
        return emailRecordMapper.selectLatestByOrderId(tenantId, orderId)
                .map(this::toDomain);
    }

    @Override
    public void updateResult(Long tenantId, Long emailRecordId, EmailRecordStatus status, String responseMessage) {
        emailRecordMapper.updateResult(tenantId, emailRecordId, status.name(), responseMessage);
    }

    private EmailRecordDO toDataObject(EmailRecord emailRecord) {
        EmailRecordDO emailRecordDO = new EmailRecordDO();
        emailRecordDO.setId(emailRecord.getId());
        emailRecordDO.setTenantId(emailRecord.getTenantId());
        emailRecordDO.setOrderId(emailRecord.getOrderId());
        emailRecordDO.setTemplateCode(emailRecord.getTemplateCode());
        emailRecordDO.setRecipient(emailRecord.getRecipient());
        emailRecordDO.setStatus(emailRecord.getStatus().name());
        emailRecordDO.setResponseMessage(emailRecord.getResponseMessage());
        emailRecordDO.setCreatedAt(emailRecord.getCreatedAt());
        emailRecordDO.setUpdatedAt(emailRecord.getUpdatedAt());
        return emailRecordDO;
    }

    private EmailRecord toDomain(EmailRecordDO emailRecordDO) {
        return new EmailRecord(
                emailRecordDO.getId(),
                emailRecordDO.getTenantId(),
                emailRecordDO.getOrderId(),
                emailRecordDO.getTemplateCode(),
                emailRecordDO.getRecipient(),
                EmailRecordStatus.valueOf(emailRecordDO.getStatus()),
                emailRecordDO.getResponseMessage(),
                emailRecordDO.getCreatedAt(),
                emailRecordDO.getUpdatedAt()
        );
    }
}
