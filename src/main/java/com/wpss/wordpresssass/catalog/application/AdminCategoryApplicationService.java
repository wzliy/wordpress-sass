package com.wpss.wordpresssass.catalog.application;

import com.wpss.wordpresssass.catalog.application.command.CreateCategoryCommand;
import com.wpss.wordpresssass.catalog.application.dto.CategoryDto;
import com.wpss.wordpresssass.catalog.domain.Category;
import com.wpss.wordpresssass.catalog.domain.CategoryRepository;
import com.wpss.wordpresssass.catalog.domain.CategoryStatus;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public AdminCategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryDto createCategory(CreateCategoryCommand command) {
        Long tenantId = requireTenantId();
        String normalizedSlug = normalizeSlug(command.name(), command.slug());
        if (categoryRepository.existsBySlug(tenantId, normalizedSlug)) {
            throw new BusinessException("Category slug already exists");
        }
        Category category = Category.create(tenantId, command.name().trim(), normalizedSlug);
        return CategoryDto.from(categoryRepository.save(category));
    }

    public List<CategoryDto> listCategories() {
        Long tenantId = requireTenantId();
        return categoryRepository.findByTenantId(tenantId)
                .stream()
                .map(CategoryDto::from)
                .toList();
    }

    public CategoryDto enableCategory(Long categoryId) {
        return updateStatus(categoryId, CategoryStatus.ACTIVE);
    }

    public CategoryDto disableCategory(Long categoryId) {
        return updateStatus(categoryId, CategoryStatus.INACTIVE);
    }

    private CategoryDto updateStatus(Long categoryId, CategoryStatus targetStatus) {
        Long tenantId = requireTenantId();
        Category category = categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new BusinessException("Category not found"));
        categoryRepository.updateStatus(categoryId, tenantId, targetStatus);
        return CategoryDto.from(category.withStatus(targetStatus));
    }

    private String normalizeSlug(String name, String rawSlug) {
        String source = rawSlug == null || rawSlug.isBlank() ? name : rawSlug;
        String normalized = source == null ? "" : source.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (normalized.isBlank()) {
            throw new BusinessException("Category slug must contain letters or numbers");
        }
        return normalized;
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
