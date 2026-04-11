package com.wpss.wordpresssass.email.domain;

import com.wpss.wordpresssass.order.domain.Order;

public interface EmailSender {

    String providerCode();

    EmailSendResult send(Order order, EmailRecord emailRecord);
}
