package com.wpss.wordpresssass.site.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfig;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteHomepageConfigDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteHomepageConfigMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MybatisSiteHomepageConfigRepository implements SiteHomepageConfigRepository {

    private final SiteHomepageConfigMapper siteHomepageConfigMapper;
    private final ObjectMapper objectMapper;

    public MybatisSiteHomepageConfigRepository(SiteHomepageConfigMapper siteHomepageConfigMapper,
                                               ObjectMapper objectMapper) {
        this.siteHomepageConfigMapper = siteHomepageConfigMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveDefaultForSite(Site site, SiteTemplate siteTemplate) {
        if (existsBySite(site.getTenantId(), site.getId())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SiteHomepageConfigDO siteHomepageConfigDO = new SiteHomepageConfigDO();
        siteHomepageConfigDO.setTenantId(site.getTenantId());
        siteHomepageConfigDO.setSiteId(site.getId());
        siteHomepageConfigDO.setConfigJson(writeJson(defaultConfig(site, siteTemplate)));
        siteHomepageConfigDO.setCreatedAt(now);
        siteHomepageConfigDO.setUpdatedAt(now);
        siteHomepageConfigMapper.insert(siteHomepageConfigDO);
    }

    @Override
    public void saveOrUpdateConfig(Long tenantId, Long siteId, String configJson) {
        Optional<SiteHomepageConfigDO> existing = siteHomepageConfigMapper.selectBySite(tenantId, siteId);
        if (existing.isPresent()) {
            siteHomepageConfigMapper.updateConfig(tenantId, siteId, configJson);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        SiteHomepageConfigDO siteHomepageConfigDO = new SiteHomepageConfigDO();
        siteHomepageConfigDO.setTenantId(tenantId);
        siteHomepageConfigDO.setSiteId(siteId);
        siteHomepageConfigDO.setConfigJson(configJson);
        siteHomepageConfigDO.setCreatedAt(now);
        siteHomepageConfigDO.setUpdatedAt(now);
        siteHomepageConfigMapper.insert(siteHomepageConfigDO);
    }

    @Override
    public boolean existsBySite(Long tenantId, Long siteId) {
        return siteHomepageConfigMapper.countBySite(tenantId, siteId) > 0;
    }

    @Override
    public Optional<SiteHomepageConfig> findBySite(Long tenantId, Long siteId) {
        return siteHomepageConfigMapper.selectBySite(tenantId, siteId)
                .map(this::toDomain);
    }

    private SiteHomepageConfig toDomain(SiteHomepageConfigDO siteHomepageConfigDO) {
        return new SiteHomepageConfig(
                siteHomepageConfigDO.getId(),
                siteHomepageConfigDO.getTenantId(),
                siteHomepageConfigDO.getSiteId(),
                siteHomepageConfigDO.getConfigJson(),
                siteHomepageConfigDO.getCreatedAt(),
                siteHomepageConfigDO.getUpdatedAt()
        );
    }

    private Map<String, Object> defaultConfig(Site site, SiteTemplate siteTemplate) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("siteCode", site.getSiteCode());
        config.put("templateCode", siteTemplate == null ? null : siteTemplate.getCode());
        config.put("themeColor", site.getThemeColor());
        config.put("logoUrl", site.getLogoUrl());
        config.put("bannerTitle", site.getBannerTitle());
        config.put("bannerSubtitle", site.getBannerSubtitle());
        config.put("featuredProductIds", List.of());
        config.put("menuItems", defaultMenuItems(siteTemplate));

        Map<String, Object> locale = new LinkedHashMap<>();
        locale.put("countryCode", site.getCountryCode());
        locale.put("languageCode", site.getLanguageCode());
        locale.put("currencyCode", site.getCurrencyCode());
        config.put("locale", locale);
        return config;
    }

    private List<Map<String, String>> defaultMenuItems(SiteTemplate siteTemplate) {
        if (siteTemplate != null && "brand-showcase-global".equals(siteTemplate.getCode())) {
            return List.of(
                    Map.of("label", "Home", "path", "/"),
                    Map.of("label", "Story", "path", "/story"),
                    Map.of("label", "Contact", "path", "/contact")
            );
        }
        return List.of(
                Map.of("label", "Home", "path", "/"),
                Map.of("label", "Catalog", "path", "/category/all"),
                Map.of("label", "Track Order", "path", "/track")
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize homepage config defaults");
        }
    }
}
