package com.wpss.wordpresssass.payment.infrastructure.provider;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.payment.domain.PaymentCallbackResult;
import com.wpss.wordpresssass.payment.domain.PaymentInitiation;
import com.wpss.wordpresssass.payment.domain.PaymentProvider;
import com.wpss.wordpresssass.payment.domain.PaymentRecord;
import org.springframework.stereotype.Component;

@Component
public class StripePaymentProviderStub implements PaymentProvider {

    @Override
    public String providerCode() {
        return "STRIPE";
    }

    @Override
    public PaymentInitiation initiate(Order order, PaymentRecord paymentRecord) {
        throw new BusinessException("Stripe provider is not implemented yet");
    }

    @Override
    public PaymentCallbackResult handleCallback(Order order, PaymentRecord paymentRecord, String callbackPayload) {
        throw new BusinessException("Stripe provider is not implemented yet");
    }
}
