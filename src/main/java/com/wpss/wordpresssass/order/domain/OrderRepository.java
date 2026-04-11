package com.wpss.wordpresssass.order.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByIdAndTenantId(Long tenantId, Long orderId);

    Optional<Order> findByOrderNo(Long tenantId, String orderNo);

    Optional<Order> findByOrderNo(Long tenantId, Long siteId, String orderNo);

    void updatePaymentStatus(Long tenantId, Long orderId, PaymentStatus paymentStatus);

    void updateShippingStatus(Long tenantId, Long orderId, ShippingStatus shippingStatus);

    List<Order> findBySite(Long tenantId,
                           Long siteId,
                           String orderNo,
                           String orderStatus,
                           String paymentStatus,
                           LocalDateTime createdFrom,
                           LocalDateTime createdTo);
}
