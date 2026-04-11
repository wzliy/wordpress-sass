package com.wpss.wordpresssass.page.infrastructure.mapper;

import com.wpss.wordpresssass.page.infrastructure.dataobject.PageDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PageMapper {

    @Insert("""
            INSERT INTO page (tenant_id, site_id, page_key, page_name, page_type, status,
                              current_version_id, published_version_id, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{pageKey}, #{pageName}, #{pageType}, #{status},
                    #{currentVersionId}, #{publishedVersionId}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PageDO pageDO);

    @Update("""
            UPDATE page
            SET page_name = #{pageName},
                page_type = #{pageType},
                status = #{status},
                current_version_id = #{currentVersionId},
                published_version_id = #{publishedVersionId},
                updated_at = #{updatedAt}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int update(PageDO pageDO);

    @Select("""
            SELECT id, tenant_id, site_id, page_key, page_name, page_type, status,
                   current_version_id, published_version_id, created_at, updated_at
            FROM page
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId} AND page_key = #{pageKey}
            LIMIT 1
            """)
    Optional<PageDO> selectBySiteAndPageKey(@Param("tenantId") Long tenantId,
                                            @Param("siteId") Long siteId,
                                            @Param("pageKey") String pageKey);

    @Select("""
            SELECT id, tenant_id, site_id, page_key, page_name, page_type, status,
                   current_version_id, published_version_id, created_at, updated_at
            FROM page
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId}
            ORDER BY created_at ASC, id ASC
            """)
    List<PageDO> selectBySite(@Param("tenantId") Long tenantId, @Param("siteId") Long siteId);
}
