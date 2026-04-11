package com.wpss.wordpresssass.shipping.infrastructure.mapper;

import com.wpss.wordpresssass.shipping.infrastructure.dataobject.ShipmentRecordDO;
import com.wpss.wordpresssass.shipping.infrastructure.dataobject.ShipmentRecordSummaryDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ShipmentRecordMapper {

    @Insert("""
            INSERT INTO shipment_record (
                tenant_id, order_id, procurement_status, shipment_status, tracking_no, carrier, failure_reason, created_at, updated_at
            )
            VALUES (
                #{tenantId}, #{orderId}, #{procurementStatus}, #{shipmentStatus}, #{trackingNo}, #{carrier}, #{failureReason}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ShipmentRecordDO shipmentRecordDO);

    @Select("""
            SELECT id, tenant_id, order_id, procurement_status, shipment_status, tracking_no, carrier, failure_reason, created_at, updated_at
            FROM shipment_record
            WHERE tenant_id = #{tenantId} AND order_id = #{orderId}
            LIMIT 1
            """)
    Optional<ShipmentRecordDO> selectByOrderId(@Param("tenantId") Long tenantId,
                                               @Param("orderId") Long orderId);

    @Select("""
            <script>
            SELECT o.id AS order_id,
                   o.site_id AS site_id,
                   o.order_no AS order_no,
                   CONCAT(o.customer_first_name, ' ', o.customer_last_name) AS customer_name,
                   o.customer_email AS customer_email,
                   COALESCE(s.procurement_status, 'NOT_ORDERED') AS procurement_status,
                   COALESCE(s.shipment_status, 'NOT_SHIPPED') AS shipment_status,
                   s.tracking_no AS tracking_no,
                   s.carrier AS carrier,
                   s.failure_reason AS failure_reason,
                   COALESCE(s.updated_at, o.created_at) AS updated_at
            FROM orders o
            LEFT JOIN shipment_record s
              ON s.tenant_id = o.tenant_id AND s.order_id = o.id
            WHERE o.tenant_id = #{tenantId}
            <if test="orderNo != null and orderNo != ''">
                AND o.order_no = #{orderNo}
            </if>
            <if test="trackingNo != null and trackingNo != ''">
                AND s.tracking_no = #{trackingNo}
            </if>
            <if test="customerEmail != null and customerEmail != ''">
                AND o.customer_email = #{customerEmail}
            </if>
            ORDER BY COALESCE(s.updated_at, o.created_at) DESC, o.id DESC
            </script>
            """)
    List<ShipmentRecordSummaryDO> search(@Param("tenantId") Long tenantId,
                                         @Param("orderNo") String orderNo,
                                         @Param("trackingNo") String trackingNo,
                                         @Param("customerEmail") String customerEmail);

    @Update("""
            UPDATE shipment_record
            SET procurement_status = #{procurementStatus},
                shipment_status = #{shipmentStatus},
                tracking_no = #{trackingNo},
                carrier = #{carrier},
                failure_reason = #{failureReason},
                updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND order_id = #{orderId}
            """)
    int updateByOrderId(@Param("tenantId") Long tenantId,
                        @Param("orderId") Long orderId,
                        @Param("procurementStatus") String procurementStatus,
                        @Param("shipmentStatus") String shipmentStatus,
                        @Param("trackingNo") String trackingNo,
                        @Param("carrier") String carrier,
                        @Param("failureReason") String failureReason);
}
