package com.wpss.wordpresssass.order.infrastructure.mapper;

import com.wpss.wordpresssass.order.infrastructure.dataobject.OrderItemDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Insert("""
            INSERT INTO order_item (
                tenant_id, order_id, product_id, sku, product_title, size_value, quantity, unit_price, line_total
            )
            VALUES (
                #{tenantId}, #{orderId}, #{productId}, #{sku}, #{productTitle}, #{sizeValue}, #{quantity}, #{unitPrice}, #{lineTotal}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderItemDO orderItemDO);

    @Select("""
            SELECT id, tenant_id, order_id, product_id, sku, product_title, size_value, quantity, unit_price, line_total
            FROM order_item
            WHERE tenant_id = #{tenantId} AND order_id = #{orderId}
            ORDER BY id ASC
            """)
    List<OrderItemDO> selectByOrderId(@Param("tenantId") Long tenantId, @Param("orderId") Long orderId);
}
