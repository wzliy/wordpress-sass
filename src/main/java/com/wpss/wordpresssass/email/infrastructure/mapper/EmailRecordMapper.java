package com.wpss.wordpresssass.email.infrastructure.mapper;

import com.wpss.wordpresssass.email.infrastructure.dataobject.EmailRecordDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface EmailRecordMapper {

    @Insert("""
            INSERT INTO email_record (
                tenant_id, order_id, template_code, recipient, status, response_message, created_at, updated_at
            )
            VALUES (
                #{tenantId}, #{orderId}, #{templateCode}, #{recipient}, #{status}, #{responseMessage}, #{createdAt}, #{updatedAt}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EmailRecordDO emailRecordDO);

    @Select("""
            SELECT id, tenant_id, order_id, template_code, recipient, status, response_message, created_at, updated_at
            FROM email_record
            WHERE tenant_id = #{tenantId} AND order_id = #{orderId}
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    Optional<EmailRecordDO> selectLatestByOrderId(@Param("tenantId") Long tenantId,
                                                  @Param("orderId") Long orderId);

    @Update("""
            UPDATE email_record
            SET status = #{status}, response_message = #{responseMessage}, updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND id = #{id}
            """)
    int updateResult(@Param("tenantId") Long tenantId,
                     @Param("id") Long id,
                     @Param("status") String status,
                     @Param("responseMessage") String responseMessage);
}
