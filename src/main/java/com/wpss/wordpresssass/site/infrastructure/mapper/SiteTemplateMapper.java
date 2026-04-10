package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteTemplateDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SiteTemplateMapper {

    @Insert("""
            INSERT INTO site_template (tenant_id, code, name, category, site_type, preview_image_url,
                                       description, status, is_builtin, created_at)
            VALUES (#{tenantId}, #{code}, #{name}, #{category}, #{siteType}, #{previewImageUrl},
                    #{description}, #{status}, #{isBuiltin}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteTemplateDO siteTemplateDO);

    @Select("""
            SELECT COUNT(1) FROM site_template
            """)
    long countAll();

    @Select("""
            SELECT id, tenant_id, code, name, category, site_type, preview_image_url, description,
                   status, is_builtin, created_at
            FROM site_template
            WHERE status = 'ACTIVE'
              AND (tenant_id = 0 OR tenant_id = #{tenantId})
            ORDER BY is_builtin DESC, created_at DESC, id DESC
            """)
    List<SiteTemplateDO> selectAvailableTemplates(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, tenant_id, code, name, category, site_type, preview_image_url, description,
                   status, is_builtin, created_at
            FROM site_template
            WHERE id = #{id}
              AND status = 'ACTIVE'
              AND (tenant_id = 0 OR tenant_id = #{tenantId})
            LIMIT 1
            """)
    Optional<SiteTemplateDO> selectAccessibleById(@Param("tenantId") Long tenantId, @Param("id") Long id);

    @Select("""
            SELECT id, tenant_id, code, name, category, site_type, preview_image_url, description,
                   status, is_builtin, created_at
            FROM site_template
            WHERE code = #{code}
              AND status = 'ACTIVE'
              AND (tenant_id = 0 OR tenant_id = #{tenantId})
            ORDER BY CASE WHEN tenant_id = #{tenantId} THEN 0 ELSE 1 END, id DESC
            LIMIT 1
            """)
    Optional<SiteTemplateDO> selectAccessibleByCode(@Param("tenantId") Long tenantId, @Param("code") String code);
}
