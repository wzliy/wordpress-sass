package com.wpss.wordpresssass.cloak.infrastructure.mapper;

import com.wpss.wordpresssass.cloak.infrastructure.dataobject.CloakRuleDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CloakRuleMapper {

    @Insert("""
            INSERT INTO cloak_rule (tenant_id, site_id, rule_name, priority, status, match_mode,
                                    traffic_percentage, condition_json, result_type, result_json,
                                    version_no, created_by, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{ruleName}, #{priority}, #{status}, #{matchMode},
                    #{trafficPercentage}, #{conditionJson}, #{resultType}, #{resultJson},
                    #{versionNo}, #{createdBy}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CloakRuleDO cloakRuleDO);

    @Update("""
            UPDATE cloak_rule
            SET rule_name = #{ruleName},
                priority = #{priority},
                status = #{status},
                match_mode = #{matchMode},
                traffic_percentage = #{trafficPercentage},
                condition_json = #{conditionJson},
                result_type = #{resultType},
                result_json = #{resultJson},
                version_no = #{versionNo},
                updated_at = #{updatedAt}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int update(CloakRuleDO cloakRuleDO);

    @Select("""
            SELECT id, tenant_id, site_id, rule_name, priority, status, match_mode,
                   traffic_percentage, condition_json, result_type, result_json,
                   version_no, created_by, created_at, updated_at
            FROM cloak_rule
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<CloakRuleDO> selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, tenant_id, site_id, rule_name, priority, status, match_mode,
                   traffic_percentage, condition_json, result_type, result_json,
                   version_no, created_by, created_at, updated_at
            FROM cloak_rule
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            ORDER BY priority ASC, id ASC
            """)
    List<CloakRuleDO> selectBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);
}
