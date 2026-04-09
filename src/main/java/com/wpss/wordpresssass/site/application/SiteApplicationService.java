package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.command.AddSiteCommand;
import com.wpss.wordpresssass.site.application.command.ProvisionSiteCommand;
import com.wpss.wordpresssass.site.application.dto.SiteConnectionResultDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import com.wpss.wordpresssass.site.application.dto.SiteProvisionResultDto;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import com.wpss.wordpresssass.site.config.MultisiteProperties;
import com.wpss.wordpresssass.site.infrastructure.provision.ProvisionedSite;
import com.wpss.wordpresssass.site.infrastructure.provision.ProvisionContext;
import com.wpss.wordpresssass.site.infrastructure.provision.SiteProvisioner;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpClient;
import com.wpss.wordpresssass.site.infrastructure.wordpress.WpConnectionResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteApplicationService {

    private final SiteRepository siteRepository;
    private final WpClient wpClient;
    private final SiteProvisioner siteProvisioner;
    private final MultisiteProperties multisiteProperties;

    public SiteApplicationService(SiteRepository siteRepository, WpClient wpClient,
                                  SiteProvisioner siteProvisioner, MultisiteProperties multisiteProperties) {
        this.siteRepository = siteRepository;
        this.wpClient = wpClient;
        this.siteProvisioner = siteProvisioner;
        this.multisiteProperties = multisiteProperties;
    }

    public SiteDto addSite(AddSiteCommand command) {
        return registerSite(command);
    }

    public SiteDto registerSite(AddSiteCommand command) {
        Long tenantId = requireTenantId();
        Site site = Site.register(
                tenantId,
                command.name().trim(),
                command.baseUrl().trim(),
                command.wpUsername().trim(),
                command.appPassword().trim()
        );
        return SiteDto.from(siteRepository.save(site));
    }

    public SiteProvisionResultDto provisionSite(ProvisionSiteCommand command) {
        Long tenantId = requireTenantId();
        String domain = buildProvisionDomain(command.subdomainPrefix().trim());
        Site provisioningSite = siteRepository.save(Site.createProvisioning(tenantId, command.name().trim(), domain));

        ProvisionedSite provisionedSite = siteProvisioner.provision(
                provisioningSite,
                new ProvisionContext(
                        normalizeSubdomain(command.subdomainPrefix()),
                        command.adminEmail().trim(),
                        multisiteProperties.getNetworkDomain()
                )
        );
        Site completedSite = provisioningSite.withProvisioningCompleted(
                provisionedSite.baseUrl(),
                provisionedSite.domain(),
                provisionedSite.adminUrl(),
                provisionedSite.wpUsername(),
                provisionedSite.appPassword(),
                provisionedSite.message()
        );
        siteRepository.updateProvisionResult(completedSite);

        return new SiteProvisionResultDto(
                completedSite.getId(),
                completedSite.getDomain(),
                completedSite.getAdminUrl(),
                completedSite.getWpUsername(),
                completedSite.getAppPassword(),
                completedSite.getProvisionStatus().name(),
                completedSite.getStatusMessage()
        );
    }

    public List<SiteDto> listSites() {
        Long tenantId = requireTenantId();
        return siteRepository.findByTenantId(tenantId)
                .stream()
                .map(SiteDto::from)
                .toList();
    }

    public SiteConnectionResultDto testConnection(Long siteId) {
        Long tenantId = requireTenantId();
        Site site = siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));

        WpConnectionResult result = wpClient.testConnection(site);
        siteRepository.updateStatus(
                site.getId(),
                tenantId,
                result.success() ? SiteStatus.ENABLED : SiteStatus.DISABLED,
                result.message()
        );
        return new SiteConnectionResultDto(site.getId(), result.success(), result.message());
    }

    private String buildProvisionDomain(String subdomainPrefix) {
        String normalized = normalizeSubdomain(subdomainPrefix);
        if (normalized.isBlank()) {
            throw new BusinessException("subdomainPrefix must contain letters or numbers");
        }
        String schema = multisiteProperties.isUseHttps() ? "https://" : "http://";
        return schema + normalized + "." + multisiteProperties.getNetworkDomain();
    }

    private String normalizeSubdomain(String subdomainPrefix) {
        return subdomainPrefix.toLowerCase()
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("(^-+|-+$)", "");
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }
}
