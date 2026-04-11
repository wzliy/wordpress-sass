package com.wpss.wordpresssass.order.domain;

import java.util.List;

public interface OrderItemRepository {

    void saveBatch(List<OrderItem> items);

    List<OrderItem> findByOrderId(Long tenantId, Long orderId);
}
