package com.wpss.wordpresssass.payment.infrastructure.mapper;

import com.wpss.wordpresssass.payment.infrastructure.dataobject.PaymentRecordDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface PaymentRecordMapper {

    @Insert("""
            INSERT INTO payment_record (
                tenant_id, order_id, provider_code, payment_no, amount, currency, status, callback_payload, created_at, updated_at
            )
            VALUES (
                #{tenantId}, #{orderId}, #{providerCode}, #{paymentNo}, #{amount}, #{currency}, #{status}, #{callbackPayload}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentRecordDO paymentRecordDO);

    @Select("""
            SELECT id, tenant_id, order_id, provider_code, payment_no, amount, currency, status, callback_payload, created_at, updated_at
            FROM payment_record
            WHERE payment_no = #{paymentNo}
            LIMIT 1
            """)
    Optional<PaymentRecordDO> selectByPaymentNo(@Param("paymentNo") String paymentNo);

    @Select("""
            SELECT id, tenant_id, order_id, provider_code, payment_no, amount, currency, status, callback_payload, created_at, updated_at
            FROM payment_record
            WHERE tenant_id = #{tenantId} AND order_id = #{orderId}
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    Optional<PaymentRecordDO> selectLatestByOrderId(@Param("tenantId") Long tenantId, @Param("orderId") Long orderId);

    @Update("""
            UPDATE payment_record
            SET status = #{status}, callback_payload = #{callbackPayload}, updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND id = #{id}
            """)
    int updateCallbackResult(@Param("tenantId") Long tenantId,
                             @Param("id") Long id,
                             @Param("status") String status,
                             @Param("callbackPayload") String callbackPayload);
}
