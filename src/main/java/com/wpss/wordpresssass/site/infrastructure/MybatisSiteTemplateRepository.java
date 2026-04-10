package com.wpss.wordpresssass.site.infrastructure;

import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.domain.SiteTemplateRepository;
import com.wpss.wordpresssass.site.infrastructure.dataobject.SiteTemplateDO;
import com.wpss.wordpresssass.site.infrastructure.mapper.SiteTemplateMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MybatisSiteTemplateRepository implements SiteTemplateRepository {

    private final SiteTemplateMapper siteTemplateMapper;

    public MybatisSiteTemplateRepository(SiteTemplateMapper siteTemplateMapper) {
        this.siteTemplateMapper = siteTemplateMapper;
    }

    @Override
    public SiteTemplate save(SiteTemplate siteTemplate) {
        SiteTemplateDO siteTemplateDO = toDataObject(siteTemplate);
        siteTemplateMapper.insert(siteTemplateDO);
        return toDomain(siteTemplateDO);
    }

    @Override
    public long countAll() {
        return siteTemplateMapper.countAll();
    }

    @Override
    public List<SiteTemplate> findAvailableTemplates(Long tenantId) {
        return siteTemplateMapper.selectAvailableTemplates(tenantId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<SiteTemplate> findAccessibleById(Long tenantId, Long templateId) {
        return siteTemplateMapper.selectAccessibleById(tenantId, templateId)
                .map(this::toDomain);
    }

    @Override
    public Optional<SiteTemplate> findAccessibleByCode(Long tenantId, String code) {
        return siteTemplateMapper.selectAccessibleByCode(tenantId, code)
                .map(this::toDomain);
    }

    private SiteTemplateDO toDataObject(SiteTemplate siteTemplate) {
        SiteTemplateDO siteTemplateDO = new SiteTemplateDO();
        siteTemplateDO.setId(siteTemplate.getId());
        siteTemplateDO.setTenantId(siteTemplate.getTenantId());
        siteTemplateDO.setCode(siteTemplate.getCode());
        siteTemplateDO.setName(siteTemplate.getName());
        siteTemplateDO.setCategory(siteTemplate.getCategory());
        siteTemplateDO.setSiteType(siteTemplate.getSiteType());
        siteTemplateDO.setPreviewImageUrl(siteTemplate.getPreviewImageUrl());
        siteTemplateDO.setDescription(siteTemplate.getDescription());
        siteTemplateDO.setStatus(siteTemplate.getStatus());
        siteTemplateDO.setIsBuiltin(siteTemplate.isBuiltIn() ? 1 : 0);
        siteTemplateDO.setCreatedAt(siteTemplate.getCreatedAt());
        return siteTemplateDO;
    }

    private SiteTemplate toDomain(SiteTemplateDO siteTemplateDO) {
        return new SiteTemplate(
                siteTemplateDO.getId(),
                siteTemplateDO.getTenantId(),
                siteTemplateDO.getCode(),
                siteTemplateDO.getName(),
                siteTemplateDO.getCategory(),
                siteTemplateDO.getSiteType(),
                siteTemplateDO.getPreviewImageUrl(),
                siteTemplateDO.getDescription(),
                siteTemplateDO.getStatus(),
                siteTemplateDO.getIsBuiltin() != null && siteTemplateDO.getIsBuiltin() == 1,
                siteTemplateDO.getCreatedAt()
        );
    }
}
