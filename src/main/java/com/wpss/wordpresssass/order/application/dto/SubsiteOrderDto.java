package com.wpss.wordpresssass.order.application.dto;

import com.wpss.wordpresssass.order.domain.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubsiteOrderDto(
        Long id,
        Long siteId,
        String orderNo,
        String customerName,
        String customerEmail,
        BigDecimal totalAmount,
        String currency,
        String orderStatus,
        String paymentStatus,
        String shippingStatus,
        LocalDateTime createdAt
) {

    public static SubsiteOrderDto from(Order order) {
        return new SubsiteOrderDto(
                order.getId(),
                order.getSiteId(),
                order.getOrderNo(),
                order.getCustomerFirstName() + " " + order.getCustomerLastName(),
                order.getCustomerEmail(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getOrderStatus().name(),
                order.getPaymentStatus().name(),
                order.getShippingStatus().name(),
                order.getCreatedAt()
        );
    }
}
