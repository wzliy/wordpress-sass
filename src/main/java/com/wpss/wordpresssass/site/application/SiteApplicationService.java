package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.command.AddSiteCommand;
import com.wpss.wordpresssass.site.application.command.ProvisionSiteCommand;
import com.wpss.wordpresssass.site.application.dto.SiteConnectionResultDto;
import com.wpss.wordpresssass.site.application.dto.SiteDto;
import com.wpss.wordpresssass.site.application.dto.SiteProvisionResultDto;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteHomepageConfigRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import com.wpss.wordpresssass.site.domain.SiteSettingRepository;
import com.wpss.wordpresssass.site.domain.SiteTemplate;
import com.wpss.wordpresssass.site.domain.SiteTemplateRepository;
import com.wpss.wordpresssass.site.domain.ThemeConfigRepository;
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
    private final SiteDomainApplicationService siteDomainApplicationService;
    private final SiteHomepageConfigRepository siteHomepageConfigRepository;
    private final SiteTemplateRepository siteTemplateRepository;
    private final SiteSettingRepository siteSettingRepository;
    private final ThemeConfigRepository themeConfigRepository;
    private final WpClient wpClient;
    private final SiteProvisioner siteProvisioner;
    private final MultisiteProperties multisiteProperties;

    public SiteApplicationService(SiteRepository siteRepository,
                                  SiteDomainApplicationService siteDomainApplicationService,
                                  SiteHomepageConfigRepository siteHomepageConfigRepository,
                                  SiteTemplateRepository siteTemplateRepository,
                                  SiteSettingRepository siteSettingRepository,
                                  ThemeConfigRepository themeConfigRepository,
                                  WpClient wpClient,
                                  SiteProvisioner siteProvisioner, MultisiteProperties multisiteProperties) {
        this.siteRepository = siteRepository;
        this.siteDomainApplicationService = siteDomainApplicationService;
        this.siteHomepageConfigRepository = siteHomepageConfigRepository;
        this.siteTemplateRepository = siteTemplateRepository;
        this.siteSettingRepository = siteSettingRepository;
        this.themeConfigRepository = themeConfigRepository;
        this.wpClient = wpClient;
        this.siteProvisioner = siteProvisioner;
        this.multisiteProperties = multisiteProperties;
    }

    public SiteDto addSite(AddSiteCommand command) {
        return registerSite(command);
    }

    public SiteDto registerSite(AddSiteCommand command) {
        Long tenantId = requireTenantId();
        validateInitialDomain(command.baseUrl().trim());
        Site site = Site.register(
                tenantId,
                command.name().trim(),
                command.baseUrl().trim(),
                command.wpUsername().trim(),
                command.appPassword().trim()
        );
        Site savedSite = siteRepository.save(site);
        siteDomainApplicationService.ensurePrimaryDomain(tenantId, savedSite.getId(), savedSite.getDomain());
        siteHomepageConfigRepository.saveDefaultForSite(savedSite, null);
        return SiteDto.from(savedSite);
    }

    public SiteProvisionResultDto provisionSite(ProvisionSiteCommand command) {
        Long tenantId = requireTenantId();
        SiteTemplate siteTemplate = siteTemplateRepository.findAccessibleByCode(tenantId, command.templateCode().trim())
                .orElseThrow(() -> new BusinessException("Template not found"));
        String domain = buildProvisionDomain(command.subdomainPrefix().trim());
        validateInitialDomain(domain);
        Site provisioningSite = siteRepository.save(
                Site.createProvisioning(
                        tenantId,
                        command.name().trim(),
                        domain,
                        siteTemplate.getId(),
                        command.countryCode().trim().toUpperCase(),
                        command.languageCode().trim().toLowerCase(),
                        command.currencyCode().trim().toUpperCase()
                )
        );

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
        siteDomainApplicationService.ensurePrimaryDomain(tenantId, completedSite.getId(), completedSite.getDomain());
        siteHomepageConfigRepository.saveDefaultForSite(completedSite, siteTemplate);
        siteSettingRepository.saveDefaultForSite(completedSite, siteTemplate);
        themeConfigRepository.saveDefaultForSite(completedSite, siteTemplate);

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

    private void validateInitialDomain(String rawDomain) {
        if (siteDomainApplicationService.existsDomain(rawDomain)) {
            throw new BusinessException("Domain already bound");
        }
    }
}
