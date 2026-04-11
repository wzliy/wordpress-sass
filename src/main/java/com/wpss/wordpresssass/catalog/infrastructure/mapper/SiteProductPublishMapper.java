package com.wpss.wordpresssass.catalog.infrastructure.mapper;

import com.wpss.wordpresssass.catalog.infrastructure.dataobject.SiteProductPublishDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SiteProductPublishMapper {

    @Insert("""
            INSERT INTO site_product_publish (tenant_id, site_id, product_id, publish_status, created_at, updated_at)
            VALUES (#{tenantId}, #{siteId}, #{productId}, #{publishStatus}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SiteProductPublishDO siteProductPublishDO);

    @Select("""
            SELECT id, tenant_id, site_id, product_id, publish_status, created_at, updated_at
            FROM site_product_publish
            WHERE tenant_id = #{tenantId} AND site_id = #{siteId} AND product_id = #{productId}
            LIMIT 1
            """)
    Optional<SiteProductPublishDO> selectBySiteIdAndProductId(@Param("tenantId") Long tenantId,
                                                              @Param("siteId") Long siteId,
                                                              @Param("productId") Long productId);

    @Select("""
            SELECT id, tenant_id, site_id, product_id, publish_status, created_at, updated_at
            FROM site_product_publish
            WHERE tenant_id = #{tenantId} AND product_id = #{productId}
            ORDER BY updated_at DESC, id DESC
            """)
    List<SiteProductPublishDO> selectByProductId(@Param("tenantId") Long tenantId,
                                                 @Param("productId") Long productId);

    @Update("""
            UPDATE site_product_publish
            SET publish_status = #{status}, updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("tenantId") Long tenantId,
                     @Param("status") String status);
}
