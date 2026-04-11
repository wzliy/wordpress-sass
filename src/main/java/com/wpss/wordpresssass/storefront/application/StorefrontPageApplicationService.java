package com.wpss.wordpresssass.storefront.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.catalog.domain.Category;
import com.wpss.wordpresssass.catalog.domain.CategoryRepository;
import com.wpss.wordpresssass.catalog.domain.CategoryStatus;
import com.wpss.wordpresssass.catalog.domain.Product;
import com.wpss.wordpresssass.catalog.domain.ProductRepository;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfig;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StorefrontPageApplicationService {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final HostDomainResolver hostDomainResolver;
    private final SiteHomepageConfigRepository siteHomepageConfigRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public StorefrontPageApplicationService(HostDomainResolver hostDomainResolver,
                                            SiteHomepageConfigRepository siteHomepageConfigRepository,
                                            CategoryRepository categoryRepository,
                                            ProductRepository productRepository,
                                            ObjectMapper objectMapper) {
        this.hostDomainResolver = hostDomainResolver;
        this.siteHomepageConfigRepository = siteHomepageConfigRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<StorefrontHomeView> resolveHome(String hostHeader) {
        return hostDomainResolver.resolve(hostHeader)
                .map(this::buildHomeView);
    }

    public Optional<StorefrontCategoryView> resolveCategory(String hostHeader, String slug, String keyword) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> buildCategoryView(site, slug, keyword));
    }

    public Optional<StorefrontProductDetailView> resolveProduct(String hostHeader, Long productId) {
        return hostDomainResolver.resolve(hostHeader)
                .flatMap(site -> buildProductView(site, productId));
    }

    private StorefrontHomeView buildHomeView(Site site) {
        JsonNode config = siteHomepageConfigRepository.findBySite(site.getTenantId(), site.getId())
                .map(SiteHomepageConfig::getConfigJson)
                .map(this::readJson)
                .orElseGet(() -> objectMapper.createObjectNode());

        return new StorefrontHomeView(
                site.getId(),
                site.getSiteCode(),
                site.getName(),
                site.getName() == null || site.getName().isBlank() ? "S" : site.getName().substring(0, 1).toUpperCase(),
                site.getDomain(),
                text(config, "themeColor", site.getThemeColor() == null ? "#2563EB" : site.getThemeColor()),
                nullableText(config, "logoUrl"),
                text(config, "bannerTitle", site.getBannerTitle()),
                text(config, "bannerSubtitle", site.getBannerSubtitle()),
                site.getLanguageCode() == null ? "pending" : site.getLanguageCode(),
                site.getCurrencyCode() == null ? "pending" : site.getCurrencyCode(),
                menuItems(config),
                featuredProductIds(site, config)
        );
    }

    private Optional<StorefrontCategoryView> buildCategoryView(Site site, String slug, String keyword) {
        String normalizedSlug = normalizeCategorySlug(slug);
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Category> activeCategories = categoryRepository.findByTenantId(site.getTenantId())
                .stream()
                .filter(category -> category.getStatus() == CategoryStatus.ACTIVE)
                .toList();
        Long categoryId = null;
        String currentCategoryName = "All Products";

        if (!"all".equals(normalizedSlug)) {
            Category category = activeCategories.stream()
                    .filter(item -> item.getSlug().equals(normalizedSlug))
                    .findFirst()
                    .orElse(null);
            if (category == null) {
                return Optional.empty();
            }
            categoryId = category.getId();
            currentCategoryName = category.getName();
        }

        List<Product> products = productRepository.findPublishedBySite(
                site.getTenantId(),
                site.getId(),
                categoryId,
                normalizedKeyword
        );
        Map<Long, String> categoryNames = activeCategories.stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));

        return Optional.of(new StorefrontCategoryView(
                site.getId(),
                site.getName(),
                site.getSiteCode(),
                site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                normalizedSlug,
                currentCategoryName,
                normalizedKeyword,
                activeCategories.stream()
                        .map(category -> new StorefrontCategoryView.CategoryItem(
                                category.getSlug(),
                                category.getName(),
                                category.getSlug().equals(normalizedSlug)
                        ))
                        .toList(),
                products.stream()
                        .map(product -> new StorefrontCategoryView.ProductCard(
                                product.getId(),
                                product.getSku(),
                                product.getTitle(),
                                categoryNames.get(product.getCategoryId()),
                                product.getCoverImage(),
                                product.getPrice(),
                                product.getCompareAtPrice()
                        ))
                        .toList()
        ));
    }

    private Optional<StorefrontProductDetailView> buildProductView(Site site, Long productId) {
        return productRepository.findVisibleBySiteAndId(site.getTenantId(), site.getId(), productId)
                .map(product -> {
                    Category category = categoryRepository.findByIdAndTenantId(product.getCategoryId(), site.getTenantId())
                            .orElse(null);
                    List<String> galleryImages = readStringList(product.getGalleryJson());
                    List<String> orderedGallery = new ArrayList<>();
                    if (product.getCoverImage() != null && !product.getCoverImage().isBlank()) {
                        orderedGallery.add(product.getCoverImage());
                    }
                    orderedGallery.addAll(galleryImages.stream()
                            .filter(url -> url != null && !url.isBlank())
                            .filter(url -> !url.equals(product.getCoverImage()))
                            .toList());
                    return new StorefrontProductDetailView(
                            site.getId(),
                            site.getName(),
                            site.getSiteCode(),
                            site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                            product.getId(),
                            product.getSku(),
                            product.getTitle(),
                            category == null ? null : category.getName(),
                            category == null ? null : category.getSlug(),
                            product.getCoverImage(),
                            orderedGallery,
                            product.getDescriptionHtml(),
                            readStringList(product.getSizesJson()),
                            product.getPrice(),
                            product.getCompareAtPrice()
                    );
                });
    }

    private JsonNode readJson(String configJson) {
        try {
            return objectMapper.readTree(configJson);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse homepage config");
        }
    }

    private List<StorefrontHomeView.MenuItem> menuItems(JsonNode config) {
        List<StorefrontHomeView.MenuItem> items = new ArrayList<>();
        JsonNode menuItems = config.path("menuItems");
        if (menuItems.isArray()) {
            for (JsonNode item : menuItems) {
                String label = nullableText(item, "label");
                String path = nullableText(item, "path");
                if (label != null && path != null) {
                    items.add(new StorefrontHomeView.MenuItem(label, path));
                }
            }
        }
        if (!items.isEmpty()) {
            return items;
        }
        return List.of(
                new StorefrontHomeView.MenuItem("Home", "/"),
                new StorefrontHomeView.MenuItem("Catalog", "/category/all"),
                new StorefrontHomeView.MenuItem("Track Order", "/track")
        );
    }

    private List<String> featuredProductIds(Site site, JsonNode config) {
        List<String> ids = new ArrayList<>();
        JsonNode featuredProductIds = config.path("featuredProductIds");
        if (featuredProductIds.isArray()) {
            for (JsonNode featuredProductId : featuredProductIds) {
                if (!featuredProductId.isNull()) {
                    String value = featuredProductId.asText();
                    Long parsedId = parseProductId(value);
                    if (parsedId != null && isPublishedForSite(site, parsedId)) {
                        ids.add(value);
                    }
                }
            }
        }
        return ids;
    }

    private boolean isPublishedForSite(Site site, Long productId) {
        return productRepository.findVisibleBySiteAndId(site.getTenantId(), site.getId(), productId).isPresent();
    }

    private Long parseProductId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(rawValue.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeCategorySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            return "all";
        }
        return slug.trim().toLowerCase();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isBlank() ? null : normalized;
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

    private String text(JsonNode node, String fieldName, String fallback) {
        String value = nullableText(node, fieldName);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String nullableText(JsonNode node, String fieldName) {
        JsonNode child = node.path(fieldName);
        if (child.isMissingNode() || child.isNull()) {
            return null;
        }
        String value = child.asText();
        return value == null || value.isBlank() ? null : value;
    }
}
