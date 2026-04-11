package com.wpss.wordpresssass.site.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteSetting;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteSettingDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteSettingMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class MybatisSiteSettingRepository implements SiteSettingRepository {

    private final SiteSettingMapper siteSettingMapper;
    private final ObjectMapper objectMapper;

    public MybatisSiteSettingRepository(SiteSettingMapper siteSettingMapper, ObjectMapper objectMapper) {
        this.siteSettingMapper = siteSettingMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public void saveDefaultForSite(Site site, SiteTemplate siteTemplate) {
        if (existsBySite(site.getTenantId(), site.getId())) {
            return;
        }

        SiteSettingDO siteSettingDO = new SiteSettingDO();
        siteSettingDO.setTenantId(site.getTenantId());
        siteSettingDO.setSiteId(site.getId());
        siteSettingDO.setPageSkeletonJson(writeJson(defaultPages(siteTemplate)));
        siteSettingDO.setDefaultConfigJson(writeJson(defaultConfig(site, siteTemplate)));
        siteSettingDO.setCreatedAt(LocalDateTime.now());
        siteSettingDO.setUpdatedAt(LocalDateTime.now());
        siteSettingMapper.insert(siteSettingDO);
    }

    @Override
    public boolean existsBySite(Long tenantId, Long siteId) {
        return siteSettingMapper.countBySite(tenantId, siteId) > 0;
    }

    @Override
    public Optional<SiteSetting> findBySite(Long tenantId, Long siteId) {
        return siteSettingMapper.selectBySite(tenantId, siteId)
                .map(this::toDomain);
    }

    @Override
    public void saveOrUpdateDefaultConfig(Long tenantId, Long siteId, String defaultConfigJson) {
        Optional<SiteSettingDO> existing = siteSettingMapper.selectBySite(tenantId, siteId);
        if (existing.isPresent()) {
            siteSettingMapper.updateDefaultConfig(tenantId, siteId, defaultConfigJson);
            return;
        }

        SiteSettingDO siteSettingDO = new SiteSettingDO();
        siteSettingDO.setTenantId(tenantId);
        siteSettingDO.setSiteId(siteId);
        siteSettingDO.setPageSkeletonJson("[]");
        siteSettingDO.setDefaultConfigJson(defaultConfigJson);
        siteSettingDO.setCreatedAt(LocalDateTime.now());
        siteSettingDO.setUpdatedAt(LocalDateTime.now());
        siteSettingMapper.insert(siteSettingDO);
    }

    private List<Map<String, String>> defaultPages(SiteTemplate siteTemplate) {
        if (siteTemplate != null && "brand-showcase-global".equals(siteTemplate.getCode())) {
            return List.of(
                    Map.of("key", "home", "name", "品牌首页", "type", "HOME"),
                    Map.of("key", "product", "name", "商品详情页", "type", "PRODUCT"),
                    Map.of("key", "checkout", "name", "结账页", "type", "CHECKOUT"),
                    Map.of("key", "success", "name", "支付成功页", "type", "SUCCESS"),
                    Map.of("key", "about", "name", "品牌故事", "type", "CONTENT"),
                    Map.of("key", "contact", "name", "联系页面", "type", "CONTACT")
            );
        }
        return List.of(
                Map.of("key", "home", "name", "默认首页", "type", "HOME"),
                Map.of("key", "product", "name", "商品详情页", "type", "PRODUCT"),
                Map.of("key", "checkout", "name", "结账页", "type", "CHECKOUT"),
                Map.of("key", "success", "name", "支付成功页", "type", "SUCCESS")
        );
    }

    private SiteSetting toDomain(SiteSettingDO siteSettingDO) {
        return new SiteSetting(
                siteSettingDO.getTenantId(),
                siteSettingDO.getSiteId(),
                siteSettingDO.getPageSkeletonJson(),
                siteSettingDO.getDefaultConfigJson(),
                siteSettingDO.getCreatedAt(),
                siteSettingDO.getUpdatedAt()
        );
    }

    private Map<String, Object> defaultConfig(Site site, SiteTemplate siteTemplate) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("templateCode", resolveTemplateCode(siteTemplate));
        config.put("templateName", resolveTemplateName(siteTemplate));
        config.put("countryCode", site.getCountryCode());
        config.put("languageCode", site.getLanguageCode());
        config.put("currencyCode", site.getCurrencyCode());
        config.put("paymentMode", "MANUAL_REVIEW");
        config.put("trackingMode", "BASIC_PIXEL");
        return config;
    }

    private String resolveTemplateCode(SiteTemplate siteTemplate) {
        return siteTemplate == null ? "manual-default" : siteTemplate.getCode();
    }

    private String resolveTemplateName(SiteTemplate siteTemplate) {
        return siteTemplate == null ? "Manual Default" : siteTemplate.getName();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize site setting defaults");
        }
    }
}
