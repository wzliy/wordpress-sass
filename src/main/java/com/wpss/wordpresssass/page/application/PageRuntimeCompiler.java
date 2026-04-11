package com.wpss.wordpresssass.page.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.page.domain.Page;
import com.wpss.wordpresssass.site.domain.Site;
import org.springframework.stereotype.Component;

@Component
public class PageRuntimeCompiler {

    private final ObjectMapper objectMapper;

    public PageRuntimeCompiler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode compile(String pageKey, String layoutJson, Site site, String baseRuntimeJson) {
        String normalizedPageKey = normalizePageKey(pageKey);
        return switch (normalizedPageKey) {
            case Page.HOME_PAGE_KEY -> compileHome(layoutJson, site, baseRuntimeJson);
            case Page.PRODUCT_PAGE_KEY -> compileProduct(layoutJson, site);
            case Page.CHECKOUT_PAGE_KEY -> compileCheckout(layoutJson, site);
            case Page.SUCCESS_PAGE_KEY -> compileSuccess(layoutJson, site);
            default -> compileGeneric(normalizedPageKey, layoutJson, site);
        };
    }

    public JsonNode compileHome(String layoutJson, Site site, String baseRuntimeJson) {
        ObjectNode runtime = readObjectNode(baseRuntimeJson);
        JsonNode layout = readJson(layoutJson);
        JsonNode sections = layout.path("sections");
        if (!sections.isArray()) {
            throw new BusinessException("Page layout sections must be an array");
        }

        for (JsonNode section : sections) {
            String type = section.path("type").asText();
            if ("hero-banner".equals(type)) {
                JsonNode props = section.path("props");
                putIfPresent(runtime, "bannerTitle", props.path("title"));
                putIfPresent(runtime, "bannerSubtitle", props.path("subtitle"));
                putIfPresent(runtime, "themeColor", props.path("themeColor"));
                putIfPresent(runtime, "logoUrl", props.path("logoUrl"));
            } else if ("top-menu".equals(type)) {
                JsonNode items = section.path("props").path("items");
                if (items.isArray()) {
                    runtime.set("menuItems", items.deepCopy());
                }
            } else if ("featured-products".equals(type)) {
                JsonNode productIds = section.path("bindings").path("productIds");
                if (productIds.isArray()) {
                    runtime.set("featuredProductIds", productIds.deepCopy());
                }
            }
        }

        if (!runtime.hasNonNull("themeColor") && site.getThemeColor() != null) {
            runtime.put("themeColor", site.getThemeColor());
        }
        if (!runtime.has("featuredProductIds")) {
            runtime.set("featuredProductIds", objectMapper.createArrayNode());
        }
        if (!runtime.has("menuItems")) {
            ArrayNode defaultMenuItems = objectMapper.createArrayNode();
            defaultMenuItems.addObject().put("label", "Home").put("path", "/");
            defaultMenuItems.addObject().put("label", "Catalog").put("path", "/category/all");
            defaultMenuItems.addObject().put("label", "Track Order").put("path", "/track");
            runtime.set("menuItems", defaultMenuItems);
        }
        return runtime;
    }

    public JsonNode compileProduct(String layoutJson, Site site) {
        JsonNode layout = readJson(layoutJson);
        ObjectNode runtime = objectMapper.createObjectNode();
        runtime.put("pageKey", Page.PRODUCT_PAGE_KEY);
        runtime.put("themeColor", defaultThemeColor(site));

        JsonNode primaryCopy = findSection(layout, "product-copy-1", "rich-text");
        JsonNode trustBadges = findSection(layout, "trust-1", "trust-badges");
        JsonNode supportCopy = findSection(layout, "shipping-1", "rich-text");

        runtime.put("pageTitle", text(primaryCopy.path("props"), "title", "Product detail promise"));
        runtime.put("body", text(primaryCopy.path("props"), "body", "Explain what makes the product worth buying."));
        runtime.set("trustBadges", readStringArray(trustBadges.path("props").path("items")));
        runtime.put("supportTitle", text(supportCopy.path("props"), "title", "Shipping & support"));
        runtime.put("supportBody", text(supportCopy.path("props"), "body", "Set delivery and support expectations here."));
        return runtime;
    }

    public JsonNode compileCheckout(String layoutJson, Site site) {
        JsonNode layout = readJson(layoutJson);
        ObjectNode runtime = objectMapper.createObjectNode();
        runtime.put("pageKey", Page.CHECKOUT_PAGE_KEY);
        runtime.put("themeColor", defaultThemeColor(site));

        JsonNode checkoutNotice = findSection(layout, "checkout-hero-1", "checkout-notice");
        JsonNode trustBadges = findSection(layout, "security-1", "trust-badges");
        JsonNode policyCopy = findSection(layout, "policy-1", "rich-text");

        runtime.put("pageTitle", text(checkoutNotice.path("props"), "title", "Secure checkout"));
        runtime.put("helperText", text(checkoutNotice.path("props"), "body", "Reassure customers before they submit payment."));
        runtime.put("submitLabel", text(checkoutNotice.path("props"), "submitLabel", "Place order"));
        runtime.set("trustBadges", readStringArray(trustBadges.path("props").path("items")));
        runtime.put("policyTitle", text(policyCopy.path("props"), "title", "Payment & shipping policy"));
        runtime.put("policyBody", text(policyCopy.path("props"), "body", "Clarify handling time, taxes and support rules."));
        return runtime;
    }

    public JsonNode compileSuccess(String layoutJson, Site site) {
        JsonNode layout = readJson(layoutJson);
        ObjectNode runtime = objectMapper.createObjectNode();
        runtime.put("pageKey", Page.SUCCESS_PAGE_KEY);

        JsonNode hero = findSection(layout, "success-hero-1", "hero-banner");
        JsonNode nextSteps = findSection(layout, "next-steps-1", "order-next-steps");
        JsonNode supportCopy = findSection(layout, "support-1", "rich-text");

        runtime.put("themeColor", text(hero.path("props"), "themeColor", defaultThemeColor(site)));
        runtime.put("bannerTitle", text(hero.path("props"), "title", "Order confirmed"));
        runtime.put("bannerSubtitle", text(hero.path("props"), "subtitle", "Tell customers what happens after they pay."));
        runtime.put("ctaLabel", text(hero.path("props"), "ctaLabel", "Continue shopping"));
        runtime.put("ctaPath", text(hero.path("props"), "ctaPath", "/category/all"));
        runtime.put("nextStepsTitle", text(nextSteps.path("props"), "title", "What happens next"));
        runtime.set("nextSteps", readStringArray(nextSteps.path("props").path("items")));
        runtime.put("supportTitle", text(supportCopy.path("props"), "title", "Need help?"));
        runtime.put("supportBody", text(supportCopy.path("props"), "body", "Add support channels and delivery reminders."));
        return runtime;
    }

    public JsonNode compileGeneric(String pageKey, String layoutJson, Site site) {
        JsonNode layout = readJson(layoutJson);
        ObjectNode runtime = objectMapper.createObjectNode();
        runtime.put("pageKey", normalizePageKey(pageKey));
        runtime.put("themeColor", defaultThemeColor(site));
        runtime.set("layout", layout);
        return runtime;
    }

    public String write(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize runtime config");
        }
    }

    private ObjectNode readObjectNode(String json) {
        JsonNode node = readJson(json);
        if (node.isObject()) {
            return (ObjectNode) node.deepCopy();
        }
        return objectMapper.createObjectNode();
    }

    private JsonNode readJson(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse page runtime payload");
        }
    }

    private void putIfPresent(ObjectNode target, String fieldName, JsonNode source) {
        if (source == null || source.isMissingNode() || source.isNull()) {
            return;
        }
        if (source.isTextual()) {
            String value = source.asText();
            if (!value.isBlank()) {
                target.put(fieldName, value);
            }
            return;
        }
        target.set(fieldName, source);
    }

    private String normalizePageKey(String pageKey) {
        return pageKey == null ? "" : pageKey.trim().toUpperCase();
    }

    private JsonNode findSection(JsonNode layout, String sectionId, String fallbackType) {
        JsonNode sections = layout.path("sections");
        if (!sections.isArray()) {
            return objectMapper.createObjectNode();
        }
        for (JsonNode section : sections) {
            if (sectionId.equals(section.path("id").asText())) {
                return section;
            }
        }
        for (JsonNode section : sections) {
            if (fallbackType.equals(section.path("type").asText())) {
                return section;
            }
        }
        return objectMapper.createObjectNode();
    }

    private ArrayNode readStringArray(JsonNode source) {
        ArrayNode result = objectMapper.createArrayNode();
        if (source.isArray()) {
            for (JsonNode item : source) {
                if (!item.isNull()) {
                    String value = item.asText();
                    if (value != null && !value.isBlank()) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    private String defaultThemeColor(Site site) {
        return site.getThemeColor() == null || site.getThemeColor().isBlank() ? "#2563EB" : site.getThemeColor();
    }

    private String text(JsonNode node, String fieldName, String fallback) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return fallback;
        }
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        String textValue = value.asText();
        return textValue == null || textValue.isBlank() ? fallback : textValue;
    }
}
