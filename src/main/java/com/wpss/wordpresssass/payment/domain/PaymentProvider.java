package com.wpss.wordpresssass.payment.domain;

import com.wpss.wordpresssass.order.domain.Order;

public interface PaymentProvider {

    String providerCode();

    PaymentInitiation initiate(Order order, PaymentRecord paymentRecord);

    PaymentCallbackResult handleCallback(Order order, PaymentRecord paymentRecord, String callbackPayload);
}
