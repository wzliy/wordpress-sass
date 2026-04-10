package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.dto.SiteTemplateDto;
import com.wpss.wordpresssass.site.domain.SiteTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteTemplateApplicationService {

    private final SiteTemplateRepository siteTemplateRepository;

    public SiteTemplateApplicationService(SiteTemplateRepository siteTemplateRepository) {
        this.siteTemplateRepository = siteTemplateRepository;
    }

    public List<SiteTemplateDto> listTemplates() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return siteTemplateRepository.findAvailableTemplates(tenantId)
                .stream()
                .map(SiteTemplateDto::from)
                .toList();
    }
}
