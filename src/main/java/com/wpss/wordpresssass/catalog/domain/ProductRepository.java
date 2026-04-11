package com.wpss.wordpresssass.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    void update(Product product);

    List<Product> findByTenantId(Long tenantId);

    Optional<Product> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Product> findVisibleBySiteAndId(Long tenantId, Long siteId, Long productId);

    List<Product> findPublishedBySite(Long tenantId, Long siteId, Long categoryId, String keyword);

    boolean existsBySku(Long tenantId, String sku);

    boolean existsBySkuExcludingId(Long tenantId, String sku, Long excludedId);

    void updateStatus(Long id, Long tenantId, ProductStatus status);
}
