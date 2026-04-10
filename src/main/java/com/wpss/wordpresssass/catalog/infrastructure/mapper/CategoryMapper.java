package com.wpss.wordpresssass.catalog.infrastructure.mapper;

import com.wpss.wordpresssass.catalog.infrastructure.dataobject.CategoryDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CategoryMapper {

    @Insert("""
            INSERT INTO category (tenant_id, name, slug, status, created_at, updated_at)
            VALUES (#{tenantId}, #{name}, #{slug}, #{status}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CategoryDO categoryDO);

    @Select("""
            SELECT id, tenant_id, name, slug, status, created_at, updated_at
            FROM category
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    List<CategoryDO> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, tenant_id, name, slug, status, created_at, updated_at
            FROM category
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<CategoryDO> selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("""
            SELECT COUNT(1)
            FROM category
            WHERE tenant_id = #{tenantId} AND slug = #{slug}
            """)
    int countBySlug(@Param("tenantId") Long tenantId, @Param("slug") String slug);

    @Update("""
            UPDATE category
            SET status = #{status}, updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int updateStatus(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("status") String status);
}
