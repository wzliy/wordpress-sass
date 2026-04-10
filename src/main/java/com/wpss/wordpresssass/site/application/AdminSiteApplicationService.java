package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.dto.AdminSiteDetailDto;
import com.wpss.wordpresssass.site.application.dto.SiteDomainDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteDomainRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminSiteApplicationService {

    private final SiteRepository siteRepository;
    private final SiteDomainRepository siteDomainRepository;

    public AdminSiteApplicationService(SiteRepository siteRepository, SiteDomainRepository siteDomainRepository) {
        this.siteRepository = siteRepository;
        this.siteDomainRepository = siteDomainRepository;
    }

    public List<SiteDto> listSites() {
        Long tenantId = requireTenantId();
        return siteRepository.findByTenantId(tenantId)
                .stream()
                .map(SiteDto::from)
                .toList();
    }

    public AdminSiteDetailDto getSiteDetail(Long siteId) {
        Long tenantId = requireTenantId();
        Site site = siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        List<SiteDomainDto> domains = siteDomainRepository.findBySiteId(tenantId, siteId)
                .stream()
                .map(SiteDomainDto::from)
                .toList();
        return new AdminSiteDetailDto(SiteDto.from(site), domains);
    }

    public AdminSiteDetailDto enableSite(Long siteId) {
        return updateSiteStatus(siteId, SiteStatus.ENABLED, "Site enabled");
    }

    public AdminSiteDetailDto disableSite(Long siteId) {
        return updateSiteStatus(siteId, SiteStatus.DISABLED, "Site disabled");
    }

    private AdminSiteDetailDto updateSiteStatus(Long siteId, SiteStatus targetStatus, String message) {
        Long tenantId = requireTenantId();
        Site site = siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        siteRepository.updateStatus(site.getId(), tenantId, targetStatus, message);
        return getSiteDetail(siteId);
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
