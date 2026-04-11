package com.wpss.wordpresssass.catalog.infrastructure;

import com.wpss.wordpresssass.catalog.domain.Category;
import com.wpss.wordpresssass.catalog.domain.CategoryRepository;
import com.wpss.wordpresssass.catalog.domain.CategoryStatus;
import com.wpss.wordpresssass.catalog.infrastructure.dataobject.CategoryDO;
import com.wpss.wordpresssass.catalog.infrastructure.mapper.CategoryMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisCategoryRepository implements CategoryRepository {

    private final CategoryMapper categoryMapper;

    public MybatisCategoryRepository(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Category save(Category category) {
        CategoryDO categoryDO = toDataObject(category);
        categoryMapper.insert(categoryDO);
        return toDomain(categoryDO);
    }

    @Override
    public List<Category> findByTenantId(Long tenantId) {
        return categoryMapper.selectByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Category> findByIdAndTenantId(Long id, Long tenantId) {
        return categoryMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Category> findBySlugAndTenantId(Long tenantId, String slug) {
        return categoryMapper.selectBySlugAndTenantId(tenantId, slug)
                .map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(Long tenantId, String slug) {
        return categoryMapper.countBySlug(tenantId, slug) > 0;
    }

    @Override
    public void updateStatus(Long id, Long tenantId, CategoryStatus status) {
        categoryMapper.updateStatus(id, tenantId, status.name());
    }

    private CategoryDO toDataObject(Category category) {
        CategoryDO categoryDO = new CategoryDO();
        categoryDO.setId(category.getId());
        categoryDO.setTenantId(category.getTenantId());
        categoryDO.setName(category.getName());
        categoryDO.setSlug(category.getSlug());
        categoryDO.setStatus(category.getStatus().name());
        categoryDO.setCreatedAt(category.getCreatedAt());
        categoryDO.setUpdatedAt(category.getUpdatedAt());
        return categoryDO;
    }

    private Category toDomain(CategoryDO categoryDO) {
        return new Category(
                categoryDO.getId(),
                categoryDO.getTenantId(),
                categoryDO.getName(),
                categoryDO.getSlug(),
                CategoryStatus.valueOf(categoryDO.getStatus()),
                categoryDO.getCreatedAt(),
                categoryDO.getUpdatedAt()
        );
    }
}
