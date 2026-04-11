package com.wpss.wordpresssass.email.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.email.domain.EmailRecord;
import com.wpss.wordpresssass.email.domain.EmailRecordRepository;
import com.wpss.wordpresssass.email.domain.EmailRecordStatus;
import com.wpss.wordpresssass.email.domain.EmailSender;
import com.wpss.wordpresssass.order.domain.Order;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderEmailApplicationService {

    private static final String ORDER_PLACED_TEMPLATE = "ORDER_PLACED";

    private final EmailRecordRepository emailRecordRepository;
    private final Map<String, EmailSender> emailSenders;

    public OrderEmailApplicationService(EmailRecordRepository emailRecordRepository,
                                        List<EmailSender> emailSenders) {
        this.emailRecordRepository = emailRecordRepository;
        this.emailSenders = emailSenders.stream()
                .collect(Collectors.toMap(EmailSender::providerCode, Function.identity()));
    }

    public void dispatchOrderPlacedEmail(Order order) {
        EmailRecord emailRecord = emailRecordRepository.save(EmailRecord.create(
                order.getTenantId(),
                order.getId(),
                ORDER_PLACED_TEMPLATE,
                order.getCustomerEmail()
        ));
        try {
            EmailSender sender = requireSender("MOCK");
            var result = sender.send(order, emailRecord);
            emailRecordRepository.updateResult(
                    order.getTenantId(),
                    emailRecord.getId(),
                    result.status(),
                    result.responseMessage()
            );
        } catch (Exception ex) {
            emailRecordRepository.updateResult(
                    order.getTenantId(),
                    emailRecord.getId(),
                    EmailRecordStatus.FAILED,
                    ex.getMessage()
            );
        }
    }

    private EmailSender requireSender(String providerCode) {
        EmailSender sender = emailSenders.get(providerCode);
        if (sender == null) {
            throw new BusinessException("Email sender not found");
        }
        return sender;
    }
}
