package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteSettingDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SiteSettingMapper {

    @Insert("""
            INSERT INTO site_setting (tenant_id, site_id, page_skeleton_json, default_config_json, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{pageSkeletonJson}, #{defaultConfigJson}, #{createdAt}, #{updatedAt})
            """)
    int insert(SiteSettingDO siteSettingDO);

    @Select("""
            SELECT COUNT(1)
            FROM site_setting
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int countBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);
}
