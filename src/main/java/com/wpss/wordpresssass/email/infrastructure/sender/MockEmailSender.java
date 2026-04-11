package com.wpss.wordpresssass.email.infrastructure.sender;

import com.wpss.wordpresssass.email.domain.EmailRecord;
import com.wpss.wordpresssass.email.domain.EmailRecordStatus;
import com.wpss.wordpresssass.email.domain.EmailSendResult;
import com.wpss.wordpresssass.email.domain.EmailSender;
import com.wpss.wordpresssass.order.domain.Order;
import org.springframework.stereotype.Component;

@Component
public class MockEmailSender implements EmailSender {

    @Override
    public String providerCode() {
        return "MOCK";
    }

    @Override
    public EmailSendResult send(Order order, EmailRecord emailRecord) {
        return new EmailSendResult(
                EmailRecordStatus.SENT,
                "Mock email accepted for order " + order.getOrderNo() + " to " + emailRecord.getRecipient()
        );
    }
}
