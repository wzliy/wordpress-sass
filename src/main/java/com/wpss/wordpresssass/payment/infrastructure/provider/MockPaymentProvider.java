package com.wpss.wordpresssass.payment.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.payment.domain.PaymentCallbackResult;
import com.wpss.wordpresssass.payment.domain.PaymentInitiation;
import com.wpss.wordpresssass.payment.domain.PaymentProvider;
import com.wpss.wordpresssass.payment.domain.PaymentRecord;
import com.wpss.wordpresssass.payment.domain.PaymentRecordStatus;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockPaymentProvider implements PaymentProvider {

    private final ObjectMapper objectMapper;

    public MockPaymentProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String providerCode() {
        return "MOCK";
    }

    @Override
    public PaymentInitiation initiate(Order order, PaymentRecord paymentRecord) {
        return new PaymentInitiation("/payments/mock/" + paymentRecord.getPaymentNo());
    }

    @Override
    public PaymentCallbackResult handleCallback(Order order, PaymentRecord paymentRecord, String callbackPayload) {
        try {
            String payload = callbackPayload == null || callbackPayload.isBlank()
                    ? objectMapper.writeValueAsString(Map.of(
                            "paymentNo", paymentRecord.getPaymentNo(),
                            "provider", providerCode(),
                            "status", "SUCCEEDED",
                            "orderNo", order.getOrderNo()
                    ))
                    : callbackPayload;
            return new PaymentCallbackResult(PaymentRecordStatus.SUCCEEDED, payload);
        } catch (Exception ex) {
            throw new BusinessException("Failed to build mock payment callback payload");
        }
    }
}
