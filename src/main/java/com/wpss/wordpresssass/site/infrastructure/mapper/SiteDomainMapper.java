package com.wpss.wordpresssass.site.infrastructure.mapper;

import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteDomainDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SiteDomainMapper {

    @Insert("""
            INSERT INTO site_domain (tenant_id, site_id, domain, is_primary, status, expiry_at, created_at)
            VALUES (#{tenantId}, #{siteId}, #{domain}, #{isPrimary}, #{status}, #{expiryAt}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteDomainDO siteDomainDO);

    @Select("""
            SELECT id, tenant_id, site_id, domain, is_primary, status, expiry_at, created_at
            FROM site_domain
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            ORDER BY is_primary DESC, created_at ASC, id ASC
            """)
    List<SiteDomainDO> selectBySiteId(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);

    @Select("""
            SELECT id, tenant_id, site_id, domain, is_primary, status, expiry_at, created_at
            FROM site_domain
            WHERE domain = #{domain}
            LIMIT 1
            """)
    Optional<SiteDomainDO> selectByDomain(@Param("domain") String domain);

    @Select("""
            SELECT id, tenant_id, site_id, domain, is_primary, status, expiry_at, created_at
            FROM site_domain
            WHERE domain = #{domain} AND status = 'ACTIVE'
            LIMIT 1
            """)
    Optional<SiteDomainDO> selectActiveByDomain(@Param("domain") String domain);

    @Select("""
            SELECT COUNT(1)
            FROM site_domain
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int countBySiteId(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);

    @Update("""
            UPDATE site_domain
            SET is_primary = 0
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            """)
    int clearPrimaryForSite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);
}
