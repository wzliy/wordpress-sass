package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SiteMapper {

    @Insert("""
            INSERT INTO site (tenant_id, site_code, name, site_type, base_url, domain, admin_url, auth_type, wp_username,
                              app_password, status, provision_status, status_msg, template_id,
                              country_code, language_code, currency_code, theme_color, logo_url, banner_title, banner_subtitle, created_at)
            VALUES (#{tenantId}, #{siteCode}, #{name}, #{siteType}, #{baseUrl}, #{domain}, #{adminUrl}, #{authType},
                    #{wpUsername}, #{appPassword}, #{status}, #{provisionStatus}, #{statusMsg}, #{templateId},
                    #{countryCode}, #{languageCode}, #{currencyCode}, #{themeColor}, #{logoUrl}, #{bannerTitle}, #{bannerSubtitle}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteDO siteDO);

    @Select("""
            SELECT id, tenant_id, site_code, name, site_type, base_url, domain, admin_url, auth_type, wp_username,
                   app_password, status, provision_status, status_msg, template_id,
                   country_code, language_code, currency_code, theme_color, logo_url, banner_title, banner_subtitle, created_at
            FROM site
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    List<SiteDO> selectByTenantId(Long tenantId);

    @Select("""
            SELECT id, tenant_id, site_code, name, site_type, base_url, domain, admin_url, auth_type, wp_username,
                   app_password, status, provision_status, status_msg, template_id,
                   country_code, language_code, currency_code, theme_color, logo_url, banner_title, banner_subtitle, created_at
            FROM site
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<SiteDO> selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Update("""
            UPDATE site
            SET status = #{status}, status_msg = #{statusMessage}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("tenantId") Long tenantId,
                     @Param("status") Integer status,
                     @Param("statusMessage") String statusMessage);

    @Update("""
            UPDATE site
            SET base_url = #{baseUrl},
                domain = #{domain},
                admin_url = #{adminUrl},
                wp_username = #{wpUsername},
                app_password = #{appPassword},
                status = #{status},
                provision_status = #{provisionStatus},
                status_msg = #{statusMsg}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int updateProvisionResult(SiteDO siteDO);

    @Update("""
            UPDATE site
            SET name = #{name},
                country_code = #{countryCode},
                language_code = #{languageCode},
                currency_code = #{currencyCode},
                logo_url = #{logoUrl},
                banner_title = #{bannerTitle},
                banner_subtitle = #{bannerSubtitle}
            WHERE tenant_id = #{tenantId} AND id = #{siteId}
            """)
    int updateSubsiteSettings(@Param("tenantId") Long tenantId,
                              @Param("siteId") Long siteId,
                              @Param("name") String name,
                              @Param("countryCode") String countryCode,
                              @Param("languageCode") String languageCode,
                              @Param("currencyCode") String currencyCode,
                              @Param("logoUrl") String logoUrl,
                              @Param("bannerTitle") String bannerTitle,
                              @Param("bannerSubtitle") String bannerSubtitle);
}
