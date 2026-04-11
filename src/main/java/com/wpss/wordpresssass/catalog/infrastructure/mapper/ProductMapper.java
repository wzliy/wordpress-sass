package com.wpss.wordpresssass.catalog.infrastructure.mapper;

import com.wpss.wordpresssass.catalog.infrastructure.dataobject.ProductDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProductMapper {

    @Insert("""
            INSERT INTO product (tenant_id, sku, title, category_id, cover_image, gallery_json, description_html,
                                 sizes_json, price, compare_at_price, status, created_at, updated_at)
            VALUES (#{tenantId}, #{sku}, #{title}, #{categoryId}, #{coverImage}, #{galleryJson}, #{descriptionHtml},
                    #{sizesJson}, #{price}, #{compareAtPrice}, #{status}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductDO productDO);

    @Update("""
            UPDATE product
            SET sku = #{sku},
                title = #{title},
                category_id = #{categoryId},
                cover_image = #{coverImage},
                gallery_json = #{galleryJson},
                description_html = #{descriptionHtml},
                sizes_json = #{sizesJson},
                price = #{price},
                compare_at_price = #{compareAtPrice},
                status = #{status},
                updated_at = #{updatedAt}
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int update(ProductDO productDO);

    @Select("""
            SELECT id, tenant_id, sku, title, category_id, cover_image, gallery_json, description_html,
                   sizes_json, price, compare_at_price, status, created_at, updated_at
            FROM product
            WHERE tenant_id = #{tenantId}
            ORDER BY created_at DESC, id DESC
            """)
    List<ProductDO> selectByTenantId(@Param("tenantId") Long tenantId);

    @Select("""
            SELECT id, tenant_id, sku, title, category_id, cover_image, gallery_json, description_html,
                   sizes_json, price, compare_at_price, status, created_at, updated_at
            FROM product
            WHERE id = #{id} AND tenant_id = #{tenantId}
            LIMIT 1
            """)
    Optional<ProductDO> selectByIdAndTenantId(@Param("id") Long id, @Param("tenantId") Long tenantId);

    @Select("""
            SELECT p.id, p.tenant_id, p.sku, p.title, p.category_id, p.cover_image, p.gallery_json, p.description_html,
                   p.sizes_json, p.price, p.compare_at_price, p.status, p.created_at, p.updated_at
            FROM product p
            INNER JOIN site_product_publish spp
                ON spp.tenant_id = p.tenant_id
               AND spp.product_id = p.id
            WHERE p.tenant_id = #{tenantId}
              AND p.id = #{productId}
              AND p.status = 'ACTIVE'
              AND spp.site_id = #{siteId}
              AND spp.publish_status = 'PUBLISHED'
            LIMIT 1
            """)
    Optional<ProductDO> selectVisibleBySiteAndId(@Param("tenantId") Long tenantId,
                                                 @Param("siteId") Long siteId,
                                                 @Param("productId") Long productId);

    @Select("""
            SELECT p.id, p.tenant_id, p.sku, p.title, p.category_id, p.cover_image, p.gallery_json, p.description_html,
                   p.sizes_json, p.price, p.compare_at_price, p.status, p.created_at, p.updated_at
            FROM product p
            INNER JOIN site_product_publish spp
                ON spp.tenant_id = p.tenant_id
               AND spp.product_id = p.id
            WHERE p.tenant_id = #{tenantId}
              AND spp.site_id = #{siteId}
              AND p.status = 'ACTIVE'
              AND spp.publish_status = 'PUBLISHED'
              AND (#{categoryId} IS NULL OR p.category_id = #{categoryId})
              AND (
                    #{keyword} IS NULL
                    OR #{keyword} = ''
                    OR LOWER(p.title) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                    OR LOWER(p.sku) LIKE CONCAT('%', LOWER(#{keyword}), '%')
                  )
            ORDER BY p.created_at DESC, p.id DESC
            """)
    List<ProductDO> selectPublishedBySite(@Param("tenantId") Long tenantId,
                                          @Param("siteId") Long siteId,
                                          @Param("categoryId") Long categoryId,
                                          @Param("keyword") String keyword);

    @Select("""
            SELECT COUNT(1)
            FROM product
            WHERE tenant_id = #{tenantId} AND sku = #{sku}
            """)
    int countBySku(@Param("tenantId") Long tenantId, @Param("sku") String sku);

    @Select("""
            SELECT COUNT(1)
            FROM product
            WHERE tenant_id = #{tenantId} AND sku = #{sku} AND id <> #{excludedId}
            """)
    int countBySkuExcludingId(@Param("tenantId") Long tenantId,
                              @Param("sku") String sku,
                              @Param("excludedId") Long excludedId);

    @Update("""
            UPDATE product
            SET status = #{status}, updated_at = CURRENT_TIMESTAMP
            WHERE id = #{id} AND tenant_id = #{tenantId}
            """)
    int updateStatus(@Param("id") Long id,
                     @Param("tenantId") Long tenantId,
                     @Param("status") String status);
}
