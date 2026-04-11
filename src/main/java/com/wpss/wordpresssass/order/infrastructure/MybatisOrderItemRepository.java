package com.wpss.wordpresssass.order.infrastructure;

import com.wpss.wordpresssass.order.domain.OrderItem;
import com.wpss.wordpresssass.order.domain.OrderItemRepository;
import com.wpss.wordpresssass.order.infrastructure.dataobject.OrderItemDO;
import com.wpss.wordpresssass.order.infrastructure.mapper.OrderItemMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MybatisOrderItemRepository implements OrderItemRepository {

    private final OrderItemMapper orderItemMapper;

    public MybatisOrderItemRepository(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public void saveBatch(List<OrderItem> items) {
        items.stream()
                .map(this::toDataObject)
                .forEach(orderItemMapper::insert);
    }

    @Override
    public List<OrderItem> findByOrderId(Long tenantId, Long orderId) {
        return orderItemMapper.selectByOrderId(tenantId, orderId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private OrderItemDO toDataObject(OrderItem item) {
        OrderItemDO orderItemDO = new OrderItemDO();
        orderItemDO.setId(item.getId());
        orderItemDO.setTenantId(item.getTenantId());
        orderItemDO.setOrderId(item.getOrderId());
        orderItemDO.setProductId(item.getProductId());
        orderItemDO.setSku(item.getSku());
        orderItemDO.setProductTitle(item.getProductTitle());
        orderItemDO.setSizeValue(item.getSizeValue());
        orderItemDO.setQuantity(item.getQuantity());
        orderItemDO.setUnitPrice(item.getUnitPrice());
        orderItemDO.setLineTotal(item.getLineTotal());
        return orderItemDO;
    }

    private OrderItem toDomain(OrderItemDO orderItemDO) {
        return new OrderItem(
                orderItemDO.getId(),
                orderItemDO.getTenantId(),
                orderItemDO.getOrderId(),
                orderItemDO.getProductId(),
                orderItemDO.getSku(),
                orderItemDO.getProductTitle(),
                orderItemDO.getSizeValue(),
                orderItemDO.getQuantity(),
                orderItemDO.getUnitPrice(),
                orderItemDO.getLineTotal()
        );
    }
}
