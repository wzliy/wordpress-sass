package com.wpss.wordpresssass.site.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.domain.ThemeConfigRepository;
import com.wpss.wordpresssass.site.infrastructure.dataobject.ThemeConfigDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.ThemeConfigMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Map;

@Repository
public class MybatisThemeConfigRepository implements ThemeConfigRepository {

    private final ThemeConfigMapper themeConfigMapper;
    private final ObjectMapper objectMapper;

    public MybatisThemeConfigRepository(ThemeConfigMapper themeConfigMapper, ObjectMapper objectMapper) {
        this.themeConfigMapper = themeConfigMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveDefaultForSite(Site site, SiteTemplate siteTemplate) {
        if (existsBySite(site.getTenantId(), site.getId())) {
            return;
        }

        ThemeConfigDO themeConfigDO = new ThemeConfigDO();
        themeConfigDO.setTenantId(site.getTenantId());
        themeConfigDO.setSiteId(site.getId());
        themeConfigDO.setConfigScope("SITE");
        themeConfigDO.setTokensJson(writeJson(defaultTokens(siteTemplate)));
        themeConfigDO.setCreatedAt(LocalDateTime.now());
        themeConfigDO.setUpdatedAt(LocalDateTime.now());
        themeConfigMapper.insert(themeConfigDO);
    }

    @Override
    public boolean existsBySite(Long tenantId, Long siteId) {
        return themeConfigMapper.countBySite(tenantId, siteId) > 0;
    }

    private Map<String, String> defaultTokens(SiteTemplate siteTemplate) {
        return switch (siteTemplate.getCode()) {
            case "beauty-flash-sale" -> Map.of(
                    "primary", "#BE185D",
                    "accent", "#FB7185",
                    "surface", "#FFF7FB",
                    "text", "#4A044E"
            );
            case "brand-showcase-global" -> Map.of(
                    "primary", "#1E3A8A",
                    "accent", "#D97706",
                    "surface", "#F8FAFC",
                    "text", "#0F172A"
            );
            default -> Map.of(
                    "primary", "#2563EB",
                    "accent", "#06B6D4",
                    "surface", "#F8FAFC",
                    "text", "#0F172A"
            );
        };
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize theme config defaults");
        }
    }
}
