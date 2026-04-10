package com.wpss.wordpresssass.storefront.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfig;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StorefrontPageApplicationService {

    private final HostDomainResolver hostDomainResolver;
    private final SiteHomepageConfigRepository siteHomepageConfigRepository;
    private final ObjectMapper objectMapper;

    public StorefrontPageApplicationService(HostDomainResolver hostDomainResolver,
                                            SiteHomepageConfigRepository siteHomepageConfigRepository,
                                            ObjectMapper objectMapper) {
        this.hostDomainResolver = hostDomainResolver;
        this.siteHomepageConfigRepository = siteHomepageConfigRepository;
        this.objectMapper = objectMapper;
    }

    public Optional<StorefrontHomeView> resolveHome(String hostHeader) {
        return hostDomainResolver.resolve(hostHeader)
                .map(this::buildHomeView);
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
                featuredProductIds(config)
        );
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

    private List<String> featuredProductIds(JsonNode config) {
        List<String> ids = new ArrayList<>();
        JsonNode featuredProductIds = config.path("featuredProductIds");
        if (featuredProductIds.isArray()) {
            for (JsonNode featuredProductId : featuredProductIds) {
                if (!featuredProductId.isNull()) {
                    ids.add(featuredProductId.asText());
                }
            }
        }
        return ids;
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
