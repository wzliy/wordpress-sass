package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteHomepageConfigDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Optional;

@Mapper
public interface SiteHomepageConfigMapper {

    @Insert("""
            INSERT INTO site_homepage_config (tenant_id, site_id, config_json, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{configJson}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteHomepageConfigDO siteHomepageConfigDO);

    @Select("""
            SELECT COUNT(1)
            FROM site_homepage_config
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int countBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);

    @Select("""
            SELECT id, tenant_id, site_id, config_json, created_at, updated_at
            FROM site_homepage_config
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            LIMIT 1
            """)
    Optional<SiteHomepageConfigDO> selectBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);

    @Update("""
            UPDATE site_homepage_config
            SET config_json = #{configJson}, updated_at = CURRENT_TIMESTAMP
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int updateConfig(@Param("tenantId") Long tenantId,
                     @Param("siteId") Long siteId,
                     @Param("configJson") String configJson);
}
