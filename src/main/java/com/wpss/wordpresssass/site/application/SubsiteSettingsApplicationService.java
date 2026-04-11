package com.wpss.wordpresssass.site.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.command.UpdateSubsiteSettingsCommand;
import com.wpss.wordpresssass.site.application.dto.SubsiteSettingsDto;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteSetting;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SubsiteSettingsApplicationService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final SiteRepository siteRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final ObjectMapper objectMapper;

    public SubsiteSettingsApplicationService(SiteRepository siteRepository,
                                             SiteSettingRepository siteSettingRepository,
                                             ObjectMapper objectMapper) {
        this.siteRepository = siteRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.objectMapper = objectMapper;
    }

    public SubsiteSettingsDto getSettings(Long siteId) {
        Long tenantId = requireTenantId();
        Site site = requireSite(tenantId, siteId);
        return toDto(site, loadConfig(tenantId, siteId));
    }

    public SubsiteSettingsDto updateSettings(UpdateSubsiteSettingsCommand command) {
        Long tenantId = requireTenantId();
        Site site = requireSite(tenantId, command.siteId());
        Map<String, Object> config = loadConfig(tenantId, command.siteId());

        String siteName = resolveRequiredField(command.siteName(), site.getName(), "siteName");
        String countryCode = resolveRequiredField(command.countryCode(), site.getCountryCode(), "countryCode");
        String languageCode = resolveRequiredField(command.languageCode(), site.getLanguageCode(), "languageCode");
        String currencyCode = resolveRequiredField(command.currencyCode(), site.getCurrencyCode(), "currencyCode");
        String logoUrl = resolveOptionalField(command.logoUrl(), site.getLogoUrl());
        String bannerTitle = resolveOptionalField(command.bannerTitle(), site.getBannerTitle());
        String bannerSubtitle = resolveOptionalField(command.bannerSubtitle(), site.getBannerSubtitle());

        siteRepository.updateSubsiteSettings(
                tenantId,
                command.siteId(),
                siteName,
                countryCode,
                languageCode,
                currencyCode,
                logoUrl,
                bannerTitle,
                bannerSubtitle
        );

        mergeOptionalConfig(config, "supportEmail", command.supportEmail());
        mergeOptionalConfig(config, "supportPhone", command.supportPhone());
        mergeOptionalConfig(config, "whatsapp", command.whatsapp());
        mergeOptionalConfig(config, "facebook", command.facebook());
        mergeOptionalConfig(config, "logisticsText", command.logisticsText());
        config.put("countryCode", countryCode);
        config.put("languageCode", languageCode);
        config.put("currencyCode", currencyCode);
        siteSettingRepository.saveOrUpdateDefaultConfig(tenantId, command.siteId(), writeJson(config));
        return getSettings(command.siteId());
    }

    private SubsiteSettingsDto toDto(Site site, Map<String, Object> config) {
        return new SubsiteSettingsDto(
                site.getId(),
                site.getSiteCode(),
                site.getName(),
                site.getBaseUrl(),
                stringValue(config.get("supportEmail")),
                stringValue(config.get("supportPhone")),
                stringValue(config.get("whatsapp")),
                stringValue(config.get("facebook")),
                site.getCurrencyCode(),
                site.getCountryCode(),
                site.getLanguageCode(),
                stringValue(config.get("logisticsText")),
                site.getLogoUrl(),
                site.getBannerTitle(),
                site.getBannerSubtitle()
        );
    }

    private Site requireSite(Long tenantId, Long siteId) {
        return siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
    }

    private Map<String, Object> loadConfig(Long tenantId, Long siteId) {
        return siteSettingRepository.findBySite(tenantId, siteId)
                .map(SiteSetting::getDefaultConfigJson)
                .map(this::readJson)
                .orElseGet(LinkedHashMap::new);
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private String resolveRequiredField(String rawValue, String currentValue, String fieldName) {
        if (rawValue == null) {
            return currentValue;
        }
        String normalized = rawValue.trim();
        if (normalized.isBlank()) {
            throw new BusinessException(fieldName + " must not be blank");
        }
        return normalized;
    }

    private String resolveOptionalField(String rawValue, String currentValue) {
        if (rawValue == null) {
            return currentValue;
        }
        String normalized = rawValue.trim();
        return normalized.isBlank() ? null : normalized;
    }

    private void mergeOptionalConfig(Map<String, Object> config, String key, String rawValue) {
        if (rawValue == null) {
            return;
        }
        String normalized = rawValue.trim();
        if (normalized.isBlank()) {
            config.remove(key);
            return;
        }
        config.put(key, normalized);
    }

    private Map<String, Object> readJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> value = objectMapper.readValue(rawJson, MAP_TYPE);
            return value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to parse site setting config");
        }
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize site setting config");
        }
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String normalized = String.valueOf(value).trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
