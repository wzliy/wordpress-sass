package com.wpss.wordpresssass.cloak.infrastructure.mapper;

import com.wpss.wordpresssass.cloak.infrastructure.dataobject.CloakHitLogDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CloakHitLogMapper {

    @Insert("""
            INSERT INTO cloak_hit_log (tenant_id, site_id, rule_id, decision, request_id,
                                       request_summary_json, matched_condition_json, created_at)
            VALUES (#{tenantId}, #{siteId}, #{ruleId}, #{decision}, #{requestId},
                    #{requestSummaryJson}, #{matchedConditionJson}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CloakHitLogDO cloakHitLogDO);

    @Select("""
            SELECT id, tenant_id, site_id, rule_id, decision, request_id,
                   request_summary_json, matched_condition_json, created_at
            FROM cloak_hit_log
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<CloakHitLogDO> selectBySite(@Param("tenantId") Long tenantId,
                                     @Param("siteId") Long siteId,
                                     @Param("limit") int limit);
}
