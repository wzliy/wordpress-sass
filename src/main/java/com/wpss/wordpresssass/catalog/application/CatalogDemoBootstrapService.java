package com.wpss.wordpresssass.catalog.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.auth.config.BootstrapAdminProperties;
import com.wpss.wordpresssass.auth.infrastructure.dataobject.TenantDO;
import com.wpss.wordpresssass.auth.infrastructure.mapper.TenantMapper;
import com.wpss.wordpresssass.catalog.domain.Category;
import com.wpss.wordpresssass.catalog.domain.CategoryRepository;
import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.catalog.domain.ProductStatus;
import com.wpss.wordpresssass.common.exception.BusinessException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Order(20)
@ConditionalOnProperty(value = "app.bootstrap.demo-catalog-enabled", havingValue = "true", matchIfMissing = true)
public class CatalogDemoBootstrapService implements ApplicationRunner {

    private final TenantMapper tenantMapper;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BootstrapAdminProperties bootstrapAdminProperties;
    private final ObjectMapper objectMapper;

    public CatalogDemoBootstrapService(TenantMapper tenantMapper,
                                       CategoryRepository categoryRepository,
                                       ProductRepository productRepository,
                                       BootstrapAdminProperties bootstrapAdminProperties,
                                       ObjectMapper objectMapper) {
        this.tenantMapper = tenantMapper;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        Optional<TenantDO> bootstrapTenant = tenantMapper.selectByName(bootstrapAdminProperties.getTenantName());
        if (bootstrapTenant.isEmpty()) {
            return;
        }

        Long tenantId = bootstrapTenant.get().getId();
        ensureCategories(tenantId);
        ensureProducts(tenantId);
    }

    private void ensureCategories(Long tenantId) {
        seedCategory(tenantId, "Outdoor Gear", "outdoor-gear");
        seedCategory(tenantId, "Beauty Essentials", "beauty-essentials");
        seedCategory(tenantId, "Home Fitness", "home-fitness");
    }

    private void ensureProducts(Long tenantId) {
        Map<String, Category> categoriesBySlug = categoryRepository.findByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(Category::getSlug, Function.identity()));

        seedProduct(
                tenantId,
                categoriesBySlug.get("outdoor-gear"),
                "DEMO-OUT-001",
                "TrailPack 40L Explorer",
                "https://cdn.demo.wpss.local/products/trailpack-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/trailpack-1.jpg",
                        "https://cdn.demo.wpss.local/products/trailpack-2.jpg"
                ),
                "<p>Carry-on friendly hiking backpack with rain cover and padded laptop sleeve.</p>",
                List.of("30L", "40L"),
                new BigDecimal("89.90"),
                new BigDecimal("129.90")
        );
        seedProduct(
                tenantId,
                categoriesBySlug.get("outdoor-gear"),
                "DEMO-OUT-002",
                "SummitShell Weather Jacket",
                "https://cdn.demo.wpss.local/products/summitshell-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/summitshell-1.jpg",
                        "https://cdn.demo.wpss.local/products/summitshell-2.jpg"
                ),
                "<p>Lightweight waterproof shell designed for spring and alpine city travel.</p>",
                List.of("S", "M", "L", "XL"),
                new BigDecimal("69.90"),
                new BigDecimal("99.90")
        );
        seedProduct(
                tenantId,
                categoriesBySlug.get("beauty-essentials"),
                "DEMO-BEA-001",
                "GlowMist Vitamin C Set",
                "https://cdn.demo.wpss.local/products/glowmist-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/glowmist-1.jpg",
                        "https://cdn.demo.wpss.local/products/glowmist-2.jpg"
                ),
                "<p>Brightening serum and mist duo for short-run flash sale landing pages.</p>",
                List.of("30ml", "50ml"),
                new BigDecimal("39.90"),
                new BigDecimal("59.90")
        );
        seedProduct(
                tenantId,
                categoriesBySlug.get("beauty-essentials"),
                "DEMO-BEA-002",
                "Midnight Repair Eye Cream",
                "https://cdn.demo.wpss.local/products/eye-cream-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/eye-cream-1.jpg",
                        "https://cdn.demo.wpss.local/products/eye-cream-2.jpg"
                ),
                "<p>Night repair formula with peptide blend and fragrance-free finish.</p>",
                List.of("15ml"),
                new BigDecimal("29.90"),
                new BigDecimal("45.90")
        );
        seedProduct(
                tenantId,
                categoriesBySlug.get("home-fitness"),
                "DEMO-FIT-001",
                "CoreBand Resistance Kit",
                "https://cdn.demo.wpss.local/products/coreband-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/coreband-1.jpg",
                        "https://cdn.demo.wpss.local/products/coreband-2.jpg"
                ),
                "<p>Portable resistance training set for apartment workouts and quick bundles.</p>",
                List.of("Light", "Medium", "Heavy"),
                new BigDecimal("49.90"),
                new BigDecimal("74.90")
        );
        seedProduct(
                tenantId,
                categoriesBySlug.get("home-fitness"),
                "DEMO-FIT-002",
                "FlexStep Recovery Roller",
                "https://cdn.demo.wpss.local/products/flexstep-cover.jpg",
                List.of(
                        "https://cdn.demo.wpss.local/products/flexstep-1.jpg",
                        "https://cdn.demo.wpss.local/products/flexstep-2.jpg"
                ),
                "<p>Textured recovery roller with guided mobility card for post-workout care.</p>",
                List.of("Standard"),
                new BigDecimal("24.90"),
                new BigDecimal("36.90")
        );
    }

    private void seedCategory(Long tenantId, String name, String slug) {
        if (categoryRepository.existsBySlug(tenantId, slug)) {
            return;
        }
        categoryRepository.save(Category.create(tenantId, name, slug));
    }

    private void seedProduct(Long tenantId,
                             Category category,
                             String sku,
                             String title,
                             String coverImage,
                             List<String> galleryImages,
                             String descriptionHtml,
                             List<String> sizes,
                             BigDecimal price,
                             BigDecimal compareAtPrice) {
        if (category == null) {
            throw new BusinessException("Seed category not found");
        }
        if (productRepository.existsBySku(tenantId, sku)) {
            return;
        }
        productRepository.save(Product.create(
                tenantId,
                sku,
                title,
                category.getId(),
                coverImage,
                writeJson(galleryImages),
                descriptionHtml,
                writeJson(sizes),
                price,
                compareAtPrice,
                ProductStatus.ACTIVE
        ));
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize demo catalog seed");
        }
    }
}
