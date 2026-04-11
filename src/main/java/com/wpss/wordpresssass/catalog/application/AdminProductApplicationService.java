package com.wpss.wordpresssass.catalog.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.catalog.application.command.CreateProductCommand;
import com.wpss.wordpresssass.catalog.application.command.UpdateProductCommand;
import com.wpss.wordpresssass.catalog.application.dto.ProductDto;
import com.wpss.wordpresssass.catalog.domain.Category;
import com.wpss.wordpresssass.catalog.domain.CategoryRepository;
import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.catalog.domain.ProductStatus;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminProductApplicationService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public AdminProductApplicationService(ProductRepository productRepository,
                                          CategoryRepository categoryRepository,
                                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    public ProductDto createProduct(CreateProductCommand command) {
        Long tenantId = requireTenantId();
        Category category = requireCategory(tenantId, command.categoryId());
        String normalizedSku = normalizeSku(command.sku());
        validatePrice(command.price(), command.compareAtPrice());
        if (productRepository.existsBySku(tenantId, normalizedSku)) {
            throw new BusinessException("Product SKU already exists");
        }

        ProductStatus status = resolveStatus(command.status(), ProductStatus.DRAFT);
        Product product = Product.create(
                tenantId,
                normalizedSku,
                command.title().trim(),
                category.getId(),
                trimToNull(command.coverImage()),
                writeStringList(command.galleryImages()),
                trimToNull(command.descriptionHtml()),
                writeStringList(command.sizes()),
                command.price(),
                command.compareAtPrice(),
                status
        );
        return toDto(productRepository.save(product), category.getName());
    }

    public ProductDto updateProduct(Long productId, UpdateProductCommand command) {
        Long tenantId = requireTenantId();
        Product existing = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException("Product not found"));
        Category category = requireCategory(tenantId, command.categoryId());
        String normalizedSku = normalizeSku(command.sku());
        validatePrice(command.price(), command.compareAtPrice());
        if (productRepository.existsBySkuExcludingId(tenantId, normalizedSku, productId)) {
            throw new BusinessException("Product SKU already exists");
        }

        Product updated = existing.withUpdated(
                normalizedSku,
                command.title().trim(),
                category.getId(),
                trimToNull(command.coverImage()),
                writeStringList(command.galleryImages()),
                trimToNull(command.descriptionHtml()),
                writeStringList(command.sizes()),
                command.price(),
                command.compareAtPrice(),
                resolveStatus(command.status(), existing.getStatus())
        );
        productRepository.update(updated);
        return toDto(updated, category.getName());
    }

    public List<ProductDto> listProducts() {
        Long tenantId = requireTenantId();
        Map<Long, String> categoryNames = categoryRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        return productRepository.findByTenantId(tenantId)
                .stream()
                .map(product -> toDto(product, categoryNames.get(product.getCategoryId())))
                .toList();
    }

    public ProductDto activateProduct(Long productId) {
        return updateStatus(productId, ProductStatus.ACTIVE);
    }

    public ProductDto deactivateProduct(Long productId) {
        return updateStatus(productId, ProductStatus.INACTIVE);
    }

    private ProductDto updateStatus(Long productId, ProductStatus targetStatus) {
        Long tenantId = requireTenantId();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new BusinessException("Product not found"));
        productRepository.updateStatus(productId, tenantId, targetStatus);
        String categoryName = categoryRepository.findByIdAndTenantId(product.getCategoryId(), tenantId)
                .map(Category::getName)
                .orElse(null);
        return toDto(product.withStatus(targetStatus), categoryName);
    }

    private ProductDto toDto(Product product, String categoryName) {
        return new ProductDto(
                product.getId(),
                product.getTenantId(),
                product.getSku(),
                product.getTitle(),
                product.getCategoryId(),
                categoryName,
                product.getCoverImage(),
                readStringList(product.getGalleryJson()),
                product.getDescriptionHtml(),
                readStringList(product.getSizesJson()),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getStatus().name(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private Category requireCategory(Long tenantId, Long categoryId) {
        return categoryRepository.findByIdAndTenantId(categoryId, tenantId)
                .orElseThrow(() -> new BusinessException("Category not found"));
    }

    private ProductStatus resolveStatus(String rawStatus, ProductStatus defaultStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return defaultStatus;
        }
        try {
            return ProductStatus.valueOf(rawStatus.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid product status");
        }
    }

    private void validatePrice(BigDecimal price, BigDecimal compareAtPrice) {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) < 0) {
            throw new BusinessException("compareAtPrice must be greater than or equal to price");
        }
    }

    private String normalizeSku(String sku) {
        String normalized = sku == null ? "" : sku.trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new BusinessException("Product SKU must not be blank");
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String writeStringList(List<String> items) {
        List<String> normalized = items == null ? List.of() : items.stream()
                .map(this::trimToNull)
                .filter(item -> item != null)
                .toList();
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize product metadata");
        }
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST);
            return values == null ? List.of() : values;
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
