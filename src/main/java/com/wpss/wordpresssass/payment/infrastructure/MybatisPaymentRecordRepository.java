package com.wpss.wordpresssass.payment.infrastructure;

import com.wpss.wordpresssass.payment.domain.PaymentRecord;
import com.wpss.wordpresssass.payment.domain.PaymentRecordRepository;
import com.wpss.wordpresssass.payment.domain.PaymentRecordStatus;
import com.wpss.wordpresssass.payment.infrastructure.dataobject.PaymentRecordDO;
import com.wpss.wordpresssass.payment.infrastructure.mapper.PaymentRecordMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MybatisPaymentRecordRepository implements PaymentRecordRepository {

    private final PaymentRecordMapper paymentRecordMapper;

    public MybatisPaymentRecordRepository(PaymentRecordMapper paymentRecordMapper) {
        this.paymentRecordMapper = paymentRecordMapper;
    }

    @Override
    public PaymentRecord save(PaymentRecord paymentRecord) {
        PaymentRecordDO paymentRecordDO = toDataObject(paymentRecord);
        paymentRecordMapper.insert(paymentRecordDO);
        return toDomain(paymentRecordDO);
    }

    @Override
    public Optional<PaymentRecord> findByPaymentNo(String paymentNo) {
        return paymentRecordMapper.selectByPaymentNo(paymentNo)
                .map(this::toDomain);
    }

    @Override
    public Optional<PaymentRecord> findLatestByOrderId(Long tenantId, Long orderId) {
        return paymentRecordMapper.selectLatestByOrderId(tenantId, orderId)
                .map(this::toDomain);
    }

    @Override
    public void updateCallbackResult(Long tenantId, Long paymentRecordId, PaymentRecordStatus status, String callbackPayload) {
        paymentRecordMapper.updateCallbackResult(tenantId, paymentRecordId, status.name(), callbackPayload);
    }

    private PaymentRecordDO toDataObject(PaymentRecord paymentRecord) {
        PaymentRecordDO paymentRecordDO = new PaymentRecordDO();
        paymentRecordDO.setId(paymentRecord.getId());
        paymentRecordDO.setTenantId(paymentRecord.getTenantId());
        paymentRecordDO.setOrderId(paymentRecord.getOrderId());
        paymentRecordDO.setProviderCode(paymentRecord.getProviderCode());
        paymentRecordDO.setPaymentNo(paymentRecord.getPaymentNo());
        paymentRecordDO.setAmount(paymentRecord.getAmount());
        paymentRecordDO.setCurrency(paymentRecord.getCurrency());
        paymentRecordDO.setStatus(paymentRecord.getStatus().name());
        paymentRecordDO.setCallbackPayload(paymentRecord.getCallbackPayload());
        paymentRecordDO.setCreatedAt(paymentRecord.getCreatedAt());
        paymentRecordDO.setUpdatedAt(paymentRecord.getUpdatedAt());
        return paymentRecordDO;
    }

    private PaymentRecord toDomain(PaymentRecordDO paymentRecordDO) {
        return new PaymentRecord(
                paymentRecordDO.getId(),
                paymentRecordDO.getTenantId(),
                paymentRecordDO.getOrderId(),
                paymentRecordDO.getProviderCode(),
                paymentRecordDO.getPaymentNo(),
                paymentRecordDO.getAmount(),
                paymentRecordDO.getCurrency(),
                PaymentRecordStatus.valueOf(paymentRecordDO.getStatus()),
                paymentRecordDO.getCallbackPayload(),
                paymentRecordDO.getCreatedAt(),
                paymentRecordDO.getUpdatedAt()
        );
    }
}
