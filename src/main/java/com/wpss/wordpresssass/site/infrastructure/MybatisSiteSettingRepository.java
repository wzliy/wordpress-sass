package com.wpss.wordpresssass.site.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteSettingDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteSettingMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
        siteSettingDO.setDefaultConfigJson(writeJson(Map.of(
                "templateCode", siteTemplate.getCode(),
                "templateName", siteTemplate.getName(),
                "countryCode", site.getCountryCode(),
                "languageCode", site.getLanguageCode(),
                "currencyCode", site.getCurrencyCode(),
                "paymentMode", "MANUAL_REVIEW",
                "trackingMode", "BASIC_PIXEL"
        )));
        siteSettingDO.setCreatedAt(LocalDateTime.now());
        siteSettingDO.setUpdatedAt(LocalDateTime.now());
        siteSettingMapper.insert(siteSettingDO);
    }

    @Override
    public boolean existsBySite(Long tenantId, Long siteId) {
        return siteSettingMapper.countBySite(tenantId, siteId) > 0;
    }

    private List<Map<String, String>> defaultPages(SiteTemplate siteTemplate) {
        if ("brand-showcase-global".equals(siteTemplate.getCode())) {
            return List.of(
                    Map.of("key", "home", "name", "品牌首页", "type", "HOME"),
                    Map.of("key", "about", "name", "品牌故事", "type", "CONTENT"),
                    Map.of("key", "contact", "name", "联系页面", "type", "CONTACT")
            );
        }
        return List.of(
                Map.of("key", "home", "name", "默认首页", "type", "HOME"),
                Map.of("key", "landing", "name", "营销落地页", "type", "LANDING"),
                Map.of("key", "checkout", "name", "结账页", "type", "CHECKOUT"),
                Map.of("key", "success", "name", "支付成功页", "type", "SUCCESS")
        );
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException("Failed to serialize site setting defaults");
        }
    }
}
