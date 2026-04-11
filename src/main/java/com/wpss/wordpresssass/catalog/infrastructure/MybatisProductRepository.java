package com.wpss.wordpresssass.catalog.infrastructure;

import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.catalog.domain.ProductStatus;
import com.wpss.wordpresssass.catalog.infrastructure.dataobject.ProductDO;
import com.wpss.wordpresssass.catalog.infrastructure.mapper.ProductMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisProductRepository implements ProductRepository {

    private final ProductMapper productMapper;

    public MybatisProductRepository(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    @Override
    public Product save(Product product) {
        ProductDO productDO = toDataObject(product);
        productMapper.insert(productDO);
        return toDomain(productDO);
    }

    @Override
    public void update(Product product) {
        productMapper.update(toDataObject(product));
    }

    @Override
    public List<Product> findByTenantId(Long tenantId) {
        return productMapper.selectByTenantId(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Product> findByIdAndTenantId(Long id, Long tenantId) {
        return productMapper.selectByIdAndTenantId(id, tenantId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Product> findVisibleBySiteAndId(Long tenantId, Long siteId, Long productId) {
        return productMapper.selectVisibleBySiteAndId(tenantId, siteId, productId)
                .map(this::toDomain);
    }

    @Override
    public List<Product> findPublishedBySite(Long tenantId, Long siteId, Long categoryId, String keyword) {
        return productMapper.selectPublishedBySite(tenantId, siteId, categoryId, keyword)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySku(Long tenantId, String sku) {
        return productMapper.countBySku(tenantId, sku) > 0;
    }

    @Override
    public boolean existsBySkuExcludingId(Long tenantId, String sku, Long excludedId) {
        return productMapper.countBySkuExcludingId(tenantId, sku, excludedId) > 0;
    }

    @Override
    public void updateStatus(Long id, Long tenantId, ProductStatus status) {
        productMapper.updateStatus(id, tenantId, status.name());
    }

    private ProductDO toDataObject(Product product) {
        ProductDO productDO = new ProductDO();
        productDO.setId(product.getId());
        productDO.setTenantId(product.getTenantId());
        productDO.setSku(product.getSku());
        productDO.setTitle(product.getTitle());
        productDO.setCategoryId(product.getCategoryId());
        productDO.setCoverImage(product.getCoverImage());
        productDO.setGalleryJson(product.getGalleryJson());
        productDO.setDescriptionHtml(product.getDescriptionHtml());
        productDO.setSizesJson(product.getSizesJson());
        productDO.setPrice(product.getPrice());
        productDO.setCompareAtPrice(product.getCompareAtPrice());
        productDO.setStatus(product.getStatus().name());
        productDO.setCreatedAt(product.getCreatedAt());
        productDO.setUpdatedAt(product.getUpdatedAt());
        return productDO;
    }

    private Product toDomain(ProductDO productDO) {
        return new Product(
                productDO.getId(),
                productDO.getTenantId(),
                productDO.getSku(),
                productDO.getTitle(),
                productDO.getCategoryId(),
                productDO.getCoverImage(),
                productDO.getGalleryJson(),
                productDO.getDescriptionHtml(),
                productDO.getSizesJson(),
                productDO.getPrice(),
                productDO.getCompareAtPrice(),
                ProductStatus.valueOf(productDO.getStatus()),
                productDO.getCreatedAt(),
                productDO.getUpdatedAt()
        );
    }
}
