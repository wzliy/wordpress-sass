package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.ThemeConfigDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ThemeConfigMapper {

    @Insert("""
            INSERT INTO theme_config (tenant_id, site_id, config_scope, tokens_json, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{configScope}, #{tokensJson}, #{createdAt}, #{updatedAt})
            """)
    int insert(ThemeConfigDO themeConfigDO);

    @Select("""
            SELECT COUNT(1)
            FROM theme_config
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int countBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);
}
