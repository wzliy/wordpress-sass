package com.wpss.wordpresssass.order.infrastructure.mapper;

import com.wpss.wordpresssass.order.infrastructure.dataobject.OrderDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Mapper
public interface OrderMapper {

    @Insert("""
            INSERT INTO orders (
                tenant_id, site_id, order_no, customer_first_name, customer_last_name, customer_email, customer_phone,
                country, state, city, address_line1, postal_code, currency,
                subtotal_amount, shipping_amount, tax_amount, total_amount,
                order_status, payment_status, shipping_status, created_at, updated_at
            )
            VALUES (
                #{tenantId}, #{siteId}, #{orderNo}, #{customerFirstName}, #{customerLastName}, #{customerEmail}, #{customerPhone},
                #{country}, #{state}, #{city}, #{addressLine1}, #{postalCode}, #{currency},
                #{subtotalAmount}, #{shippingAmount}, #{taxAmount}, #{totalAmount},
                #{orderStatus}, #{paymentStatus}, #{shippingStatus}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderDO orderDO);

    @Select("""
            SELECT id, tenant_id, site_id, order_no, customer_first_name, customer_last_name, customer_email, customer_phone,
                   country, state, city, address_line1, postal_code, currency,
                   subtotal_amount, shipping_amount, tax_amount, total_amount,
                   order_status, payment_status, shipping_status, created_at, updated_at
            FROM orders
            WHERE tenant_id = #{tenantId} AND id = #{orderId}
            LIMIT 1
            """)
    Optional<OrderDO> selectByIdAndTenantId(@Param("tenantId") Long tenantId,
                                            @Param("orderId") Long orderId);

    @Select("""
            SELECT id, tenant_id, site_id, order_no, customer_first_name, customer_last_name, customer_email, customer_phone,
                   country, state, city, address_line1, postal_code, currency,
                   subtotal_amount, shipping_amount, tax_amount, total_amount,
                   order_status, payment_status, shipping_status, created_at, updated_at
            FROM orders
            WHERE tenant_id = #{tenantId} AND order_no = #{orderNo}
            LIMIT 1
            """)
    Optional<OrderDO> selectByOrderNoAcrossSites(@Param("tenantId") Long tenantId,
                                                 @Param("orderNo") String orderNo);

    @Select("""
            SELECT id, tenant_id, site_id, order_no, customer_first_name, customer_last_name, customer_email, customer_phone,
                   country, state, city, address_line1, postal_code, currency,
                   subtotal_amount, shipping_amount, tax_amount, total_amount,
                   order_status, payment_status, shipping_status, created_at, updated_at
            FROM orders
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId} AND order_no = #{orderNo}
            LIMIT 1
            """)
    Optional<OrderDO> selectByOrderNo(@Param("tenantId") Long tenantId,
                                      @Param("siteId") Long siteId,
                                      @Param("orderNo") String orderNo);

    @Select("""
            <script>
            SELECT id, tenant_id, site_id, order_no, customer_first_name, customer_last_name, customer_email, customer_phone,
                   country, state, city, address_line1, postal_code, currency,
                   subtotal_amount, shipping_amount, tax_amount, total_amount,
                   order_status, payment_status, shipping_status, created_at, updated_at
            FROM orders
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            <if test="orderNo != null and orderNo != ''">
                AND order_no = #{orderNo}
            </if>
            <if test="orderStatus != null and orderStatus != ''">
                AND order_status = #{orderStatus}
            </if>
            <if test="paymentStatus != null and paymentStatus != ''">
                AND payment_status = #{paymentStatus}
            </if>
            <if test="createdFrom != null">
                AND created_at <![CDATA[>=]]> #{createdFrom}
            </if>
            <if test="createdTo != null">
                AND created_at <![CDATA[<=]]> #{createdTo}
            </if>
            ORDER BY created_at DESC, id DESC
            </script>
            """)
    List<OrderDO> selectBySite(@Param("tenantId") Long tenantId,
                               @Param("siteId") Long siteId,
                               @Param("orderNo") String orderNo,
                               @Param("orderStatus") String orderStatus,
                               @Param("paymentStatus") String paymentStatus,
                               @Param("createdFrom") LocalDateTime createdFrom,
                               @Param("createdTo") LocalDateTime createdTo);

    @Update("""
            UPDATE orders
            SET payment_status = #{paymentStatus},
                order_status = CASE WHEN #{paymentStatus} = 'PAID' THEN 'PAID' ELSE order_status END,
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND id = #{orderId}
            """)
    int updatePaymentStatus(@Param("tenantId") Long tenantId,
                            @Param("orderId") Long orderId,
                            @Param("paymentStatus") String paymentStatus);

    @Update("""
            UPDATE orders
            SET shipping_status = #{shippingStatus}, updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND id = #{orderId}
            """)
    int updateShippingStatus(@Param("tenantId") Long tenantId,
                             @Param("orderId") Long orderId,
                             @Param("shippingStatus") String shippingStatus);
}
