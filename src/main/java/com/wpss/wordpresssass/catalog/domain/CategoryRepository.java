package com.wpss.wordpresssass.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findByTenantId(Long tenantId);

    Optional<Category> findByIdAndTenantId(Long id, Long tenantId);

    Optional<Category> findBySlugAndTenantId(Long tenantId, String slug);

    boolean existsBySlug(Long tenantId, String slug);

    void updateStatus(Long id, Long tenantId, CategoryStatus status);
}
