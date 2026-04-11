package com.wpss.wordpresssass.order.infrastructure;

import com.wpss.wordpresssass.order.domain.Order;
import com.wpss.wordpresssass.order.domain.OrderRepository;
import com.wpss.wordpresssass.order.domain.OrderStatus;
import com.wpss.wordpresssass.order.domain.PaymentStatus;
import com.wpss.wordpresssass.order.domain.ShippingStatus;
import com.wpss.wordpresssass.order.infrastructure.dataobject.OrderDO;
import com.wpss.wordpresssass.order.infrastructure.mapper.OrderMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class MybatisOrderRepository implements OrderRepository {

    private final OrderMapper orderMapper;

    public MybatisOrderRepository(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    @Override
    public Order save(Order order) {
        OrderDO orderDO = toDataObject(order);
        orderMapper.insert(orderDO);
        return toDomain(orderDO);
    }

    @Override
    public Optional<Order> findByIdAndTenantId(Long tenantId, Long orderId) {
        return orderMapper.selectByIdAndTenantId(tenantId, orderId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
        return orderMapper.selectByOrderNoAcrossSites(tenantId, orderNo)
                .map(this::toDomain);
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, Long siteId, String orderNo) {
        return orderMapper.selectByOrderNo(tenantId, siteId, orderNo)
                .map(this::toDomain);
    }

    @Override
    public List<Order> findBySite(Long tenantId,
                                  Long siteId,
                                  String orderNo,
                                  String orderStatus,
                                  String paymentStatus,
                                  LocalDateTime createdFrom,
                                  LocalDateTime createdTo) {
        return orderMapper.selectBySite(tenantId, siteId, orderNo, orderStatus, paymentStatus, createdFrom, createdTo)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void updatePaymentStatus(Long tenantId, Long orderId, PaymentStatus paymentStatus) {
        orderMapper.updatePaymentStatus(tenantId, orderId, paymentStatus.name());
    }

    @Override
    public void updateShippingStatus(Long tenantId, Long orderId, ShippingStatus shippingStatus) {
        orderMapper.updateShippingStatus(tenantId, orderId, shippingStatus.name());
    }

    private OrderDO toDataObject(Order order) {
        OrderDO orderDO = new OrderDO();
        orderDO.setId(order.getId());
        orderDO.setTenantId(order.getTenantId());
        orderDO.setSiteId(order.getSiteId());
        orderDO.setOrderNo(order.getOrderNo());
        orderDO.setCustomerFirstName(order.getCustomerFirstName());
        orderDO.setCustomerLastName(order.getCustomerLastName());
        orderDO.setCustomerEmail(order.getCustomerEmail());
        orderDO.setCustomerPhone(order.getCustomerPhone());
        orderDO.setCountry(order.getCountry());
        orderDO.setState(order.getState());
        orderDO.setCity(order.getCity());
        orderDO.setAddressLine1(order.getAddressLine1());
        orderDO.setPostalCode(order.getPostalCode());
        orderDO.setCurrency(order.getCurrency());
        orderDO.setSubtotalAmount(order.getSubtotalAmount());
        orderDO.setShippingAmount(order.getShippingAmount());
        orderDO.setTaxAmount(order.getTaxAmount());
        orderDO.setTotalAmount(order.getTotalAmount());
        orderDO.setOrderStatus(order.getOrderStatus().name());
        orderDO.setPaymentStatus(order.getPaymentStatus().name());
        orderDO.setShippingStatus(order.getShippingStatus().name());
        orderDO.setCreatedAt(order.getCreatedAt());
        orderDO.setUpdatedAt(order.getUpdatedAt());
        return orderDO;
    }

    private Order toDomain(OrderDO orderDO) {
        return new Order(
                orderDO.getId(),
                orderDO.getTenantId(),
                orderDO.getSiteId(),
                orderDO.getOrderNo(),
                orderDO.getCustomerFirstName(),
                orderDO.getCustomerLastName(),
                orderDO.getCustomerEmail(),
                orderDO.getCustomerPhone(),
                orderDO.getCountry(),
                orderDO.getState(),
                orderDO.getCity(),
                orderDO.getAddressLine1(),
                orderDO.getPostalCode(),
                orderDO.getCurrency(),
                orderDO.getSubtotalAmount(),
                orderDO.getShippingAmount(),
                orderDO.getTaxAmount(),
                orderDO.getTotalAmount(),
                OrderStatus.valueOf(orderDO.getOrderStatus()),
                PaymentStatus.valueOf(orderDO.getPaymentStatus()),
                ShippingStatus.fromValue(orderDO.getShippingStatus()),
                orderDO.getCreatedAt(),
                orderDO.getUpdatedAt()
        );
    }
}
