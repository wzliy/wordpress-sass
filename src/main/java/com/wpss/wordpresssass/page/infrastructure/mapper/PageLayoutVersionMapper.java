package com.wpss.wordpresssass.page.infrastructure.mapper;

import com.wpss.wordpresssass.page.infrastructure.dataobject.PageLayoutVersionDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PageLayoutVersionMapper {

    @Insert("""
            INSERT INTO page_layout_version (tenant_id, site_id, page_id, version_no, version_status,
                                             schema_version, layout_json, compiled_runtime_json,
                                             version_note, created_by, created_at, published_at)
            VALUES (#{tenantId}, #{siteId}, #{pageId}, #{versionNo}, #{versionStatus},
                    #{schemaVersion}, #{layoutJson}, #{compiledRuntimeJson},
                    #{versionNote}, #{createdBy}, #{createdAt}, #{publishedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PageLayoutVersionDO versionDO);

    @Update("""
            UPDATE page_layout_version
            SET version_status = #{versionStatus},
                schema_version = #{schemaVersion},
                layout_json = #{layoutJson},
                compiled_runtime_json = #{compiledRuntimeJson},
                version_note = #{versionNote},
                created_by = #{createdBy},
                published_at = #{publishedAt}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int update(PageLayoutVersionDO versionDO);

    @Select("""
            SELECT id, tenant_id, site_id, page_id, version_no, version_status,
                   schema_version, layout_json, compiled_runtime_json, version_note,
                   created_by, created_at, published_at
            FROM page_layout_version
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<PageLayoutVersionDO> selectByIdAndTenantId(@Param("id") Long id,
                                                        @Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, tenant_id, site_id, page_id, version_no, version_status,
                   schema_version, layout_json, compiled_runtime_json, version_note,
                   created_by, created_at, published_at
            FROM page_layout_version
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId} AND page_id = #{pageId}
            ORDER BY version_no DESC, id DESC
            """)
    List<PageLayoutVersionDO> selectByPage(@Param("tenantId") Long tenantId,
                                           @Param("siteId") Long siteId,
                                           @Param("pageId") Long pageId);
}
