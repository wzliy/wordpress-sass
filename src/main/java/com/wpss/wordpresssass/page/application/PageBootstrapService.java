package com.wpss.wordpresssass.page.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.page.domain.PageLayoutVersion;
import com.wpss.wordpresssass.page.domain.PageLayoutVersionRepository;
import com.wpss.wordpresssass.page.domain.PageRepository;
import com.wpss.wordpresssass.page.domain.PageStatus;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfig;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import com.wpss.wordpresssass.site.domain.SiteSetting;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageBootstrapService {

    private static final TypeReference<List<Map<String, Object>>> PAGE_SKELETON_LIST = new TypeReference<>() {
    };

    private final PageRepository pageRepository;
    private final PageLayoutVersionRepository pageLayoutVersionRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final SiteHomepageConfigRepository siteHomepageConfigRepository;
    private final PageRuntimeCompiler pageRuntimeCompiler;
    private final ObjectMapper objectMapper;

    public PageBootstrapService(PageRepository pageRepository,
                                PageLayoutVersionRepository pageLayoutVersionRepository,
                                SiteSettingRepository siteSettingRepository,
                                SiteHomepageConfigRepository siteHomepageConfigRepository,
                                PageRuntimeCompiler pageRuntimeCompiler,
                                ObjectMapper objectMapper) {
        this.pageRepository = pageRepository;
        this.pageLayoutVersionRepository = pageLayoutVersionRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.siteHomepageConfigRepository = siteHomepageConfigRepository;
        this.pageRuntimeCompiler = pageRuntimeCompiler;
        this.objectMapper = objectMapper;
    }

    public void bootstrapDefaultPages(Site site) {
        Map<String, String> pageNames = resolvePageNames(site.getTenantId(), site.getId());
        String runtimeJson = siteHomepageConfigRepository.findBySite(site.getTenantId(), site.getId())
                .map(SiteHomepageConfig::getConfigJson)
                .orElseGet(() -> writeJson(defaultRuntimeConfig(site)));

        for (String pageKey : List.of(
                Page.HOME_PAGE_KEY,
                Page.PRODUCT_PAGE_KEY,
                Page.CHECKOUT_PAGE_KEY,
                Page.SUCCESS_PAGE_KEY
        )) {
            if (pageRepository.findBySiteAndPageKey(site.getTenantId(), site.getId(), pageKey).isPresent()) {
                continue;
            }

            String pageName = pageNames.getOrDefault(pageKey, defaultPageName(pageKey));
            Page page = pageRepository.save(Page.createSystemPage(site.getTenantId(), site.getId(), pageKey, pageName));
            String layoutJson = buildInitialLayoutJson(pageKey, site, runtimeJson);
            String compiledRuntimeJson = pageRuntimeCompiler.write(pageRuntimeCompiler.compile(pageKey, layoutJson, site, runtimeJson));
            PageLayoutVersion version = pageLayoutVersionRepository.save(
                    PageLayoutVersion.initialPublished(
                            site.getTenantId(),
                            site.getId(),
                            page.getId(),
                            layoutJson,
                            compiledRuntimeJson,
                            "Initial " + pageKey + " page bootstrap"
                    )
            );
            pageRepository.update(page.withVersionPointers(version.getId(), version.getId(), PageStatus.PUBLISHED));
        }
    }

    private Map<String, String> resolvePageNames(Long tenantId, Long siteId) {
        return siteSettingRepository.findBySite(tenantId, siteId)
                .map(SiteSetting::getPageSkeletonJson)
                .map(this::findPageNames)
                .orElseGet(HashMap::new);
    }

    private Map<String, String> findPageNames(String skeletonJson) {
        Map<String, String> pageNames = new HashMap<>();
        if (skeletonJson == null || skeletonJson.isBlank()) {
            return pageNames;
        }
        try {
            List<Map<String, Object>> items = objectMapper.readValue(skeletonJson, PAGE_SKELETON_LIST);
            for (Map<String, Object> item : items) {
                String pageKey = resolveSupportedPageKey(item);
                String pageName = stringValue(item.get("name"));
                if (pageKey != null && pageName != null && !pageName.isBlank()) {
                    pageNames.put(pageKey, pageName);
                }
            }
            return pageNames;
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse site page skeleton");
        }
    }

    private String buildInitialLayoutJson(String pageKey, Site site, String runtimeJson) {
        return switch (pageKey) {
            case Page.HOME_PAGE_KEY -> buildHomeLayoutJson(runtimeJson);
            case Page.PRODUCT_PAGE_KEY -> buildProductLayoutJson(site);
            case Page.CHECKOUT_PAGE_KEY -> buildCheckoutLayoutJson();
            case Page.SUCCESS_PAGE_KEY -> buildSuccessLayoutJson(site);
            default -> throw new BusinessException("Unsupported page key");
        };
    }

    private String buildHomeLayoutJson(String runtimeJson) {
        JsonNode runtime = readJson(runtimeJson);
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(heroSection(runtime));
        sections.add(menuSection(runtime));
        sections.add(featuredProductsSection(runtime));

        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("pageKey", Page.HOME_PAGE_KEY);
        layout.put("sections", sections);
        return writeJson(layout);
    }

    private String buildProductLayoutJson(Site site) {
        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("pageKey", Page.PRODUCT_PAGE_KEY);
        layout.put("sections", List.of(
                richTextSection(
                        "product-copy-1",
                        "Product detail promise",
                        "Use this area to explain the product angle, offer and brand fit."
                ),
                trustBadgesSection(
                        "trust-1",
                        List.of("Fast shipping", "Secure payment", "7-day support")
                ),
                richTextSection(
                        "shipping-1",
                        "Shipping & support",
                        "Set expectations for handling time, returns and customer support here."
                )
        ));
        return writeJson(layout);
    }

    private String buildCheckoutLayoutJson() {
        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("pageKey", Page.CHECKOUT_PAGE_KEY);
        layout.put("sections", List.of(
                checkoutNoticeSection(
                        "checkout-hero-1",
                        "Secure checkout",
                        "Reassure buyers before they complete payment and shipping.",
                        "Place order"
                ),
                trustBadgesSection(
                        "security-1",
                        List.of("Encrypted payment", "Address verification", "Live order support")
                ),
                richTextSection(
                        "policy-1",
                        "Payment & shipping policy",
                        "Describe tax handling, shipping windows and post-purchase support."
                )
        ));
        return writeJson(layout);
    }

    private String buildSuccessLayoutJson(Site site) {
        Map<String, Object> layout = new LinkedHashMap<>();
        layout.put("pageKey", Page.SUCCESS_PAGE_KEY);
        layout.put("sections", List.of(
                successHeroSection(site),
                orderNextStepsSection(
                        "next-steps-1",
                        "What happens next",
                        List.of("Confirmation email is sent immediately", "Warehouse reviews the order", "Tracking number follows after dispatch")
                ),
                richTextSection(
                        "support-1",
                        "Need help?",
                        "Add after-sales support, shipping reminders and upsell guidance here."
                )
        ));
        return writeJson(layout);
    }

    private Map<String, Object> heroSection(JsonNode runtime) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", "hero-1");
        section.put("type", "hero-banner");

        Map<String, Object> props = new LinkedHashMap<>();
        props.put("title", text(runtime, "bannerTitle", "Your storefront is ready"));
        props.put("subtitle", text(runtime, "bannerSubtitle", "Launch the first editable home page."));
        props.put("themeColor", text(runtime, "themeColor", "#2563EB"));
        props.put("logoUrl", nullableText(runtime, "logoUrl"));
        props.put("ctaLabel", "Browse catalog");
        props.put("ctaPath", "/category/all");

        section.put("props", props);
        return section;
    }

    private Map<String, Object> menuSection(JsonNode runtime) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", "menu-1");
        section.put("type", "top-menu");

        List<Map<String, String>> items = new ArrayList<>();
        JsonNode menuItems = runtime.path("menuItems");
        if (menuItems.isArray()) {
            for (JsonNode item : menuItems) {
                String label = nullableText(item, "label");
                String path = nullableText(item, "path");
                if (label != null && path != null) {
                    items.add(Map.of("label", label, "path", path));
                }
            }
        }
        if (items.isEmpty()) {
            items = List.of(
                    Map.of("label", "Home", "path", "/"),
                    Map.of("label", "Catalog", "path", "/category/all"),
                    Map.of("label", "Track Order", "path", "/track")
            );
        }

        section.put("props", Map.of("items", items));
        return section;
    }

    private Map<String, Object> featuredProductsSection(JsonNode runtime) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", "featured-1");
        section.put("type", "featured-products");

        List<String> productIds = new ArrayList<>();
        JsonNode featuredProductIds = runtime.path("featuredProductIds");
        if (featuredProductIds.isArray()) {
            for (JsonNode featuredProductId : featuredProductIds) {
                if (!featuredProductId.isNull() && !featuredProductId.asText().isBlank()) {
                    productIds.add(featuredProductId.asText());
                }
            }
        }

        Map<String, Object> bindings = new LinkedHashMap<>();
        bindings.put("productIds", productIds);
        section.put("bindings", bindings);
        return section;
    }

    private Map<String, Object> richTextSection(String sectionId, String title, String body) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", sectionId);
        section.put("type", "rich-text");
        section.put("props", Map.of(
                "title", title,
                "body", body
        ));
        return section;
    }

    private Map<String, Object> trustBadgesSection(String sectionId, List<String> items) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", sectionId);
        section.put("type", "trust-badges");
        section.put("props", Map.of("items", items));
        return section;
    }

    private Map<String, Object> checkoutNoticeSection(String sectionId,
                                                      String title,
                                                      String body,
                                                      String submitLabel) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", sectionId);
        section.put("type", "checkout-notice");
        section.put("props", Map.of(
                "title", title,
                "body", body,
                "submitLabel", submitLabel
        ));
        return section;
    }

    private Map<String, Object> successHeroSection(Site site) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", "success-hero-1");
        section.put("type", "hero-banner");
        section.put("props", Map.of(
                "title", "Order confirmed",
                "subtitle", "Thank the customer and set expectations for fulfillment.",
                "themeColor", site.getThemeColor() == null ? "#2563EB" : site.getThemeColor(),
                "ctaLabel", "Continue shopping",
                "ctaPath", "/category/all"
        ));
        return section;
    }

    private Map<String, Object> orderNextStepsSection(String sectionId, String title, List<String> items) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", sectionId);
        section.put("type", "order-next-steps");
        section.put("props", Map.of(
                "title", title,
                "items", items
        ));
        return section;
    }

    private JsonNode readJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse homepage runtime config");
        }
    }

    private Map<String, Object> defaultRuntimeConfig(Site site) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("themeColor", site.getThemeColor());
        config.put("logoUrl", site.getLogoUrl());
        config.put("bannerTitle", site.getBannerTitle());
        config.put("bannerSubtitle", site.getBannerSubtitle());
        config.put("featuredProductIds", List.of());
        config.put("menuItems", List.of(
                Map.of("label", "Home", "path", "/"),
                Map.of("label", "Catalog", "path", "/category/all"),
                Map.of("label", "Track Order", "path", "/track")
        ));
        return config;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize default page layout");
        }
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String resolveSupportedPageKey(Map<String, Object> item) {
        String type = normalizePageKey(stringValue(item.get("type")));
        String key = normalizePageKey(stringValue(item.get("key")));
        for (String candidate : List.of(type, key)) {
            if (Page.HOME_PAGE_KEY.equals(candidate)
                    || Page.PRODUCT_PAGE_KEY.equals(candidate)
                    || Page.CHECKOUT_PAGE_KEY.equals(candidate)
                    || Page.SUCCESS_PAGE_KEY.equals(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String defaultPageName(String pageKey) {
        return switch (pageKey) {
            case Page.HOME_PAGE_KEY -> "默认首页";
            case Page.PRODUCT_PAGE_KEY -> "商品详情页";
            case Page.CHECKOUT_PAGE_KEY -> "结账页";
            case Page.SUCCESS_PAGE_KEY -> "支付成功页";
            default -> pageKey;
        };
    }

    private String normalizePageKey(String pageKey) {
        return pageKey == null ? null : pageKey.trim().toUpperCase();
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
