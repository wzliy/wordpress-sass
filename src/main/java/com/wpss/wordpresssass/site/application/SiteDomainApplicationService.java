package com.wpss.wordpresssass.site.application;

import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import com.wpss.wordpresssass.site.application.command.BindSiteDomainCommand;
import com.wpss.wordpresssass.site.application.dto.SiteDomainDto;
import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteDomain;
import com.wpss.wordpresssass.site.domain.SiteDomainRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class SiteDomainApplicationService {

    private final SiteRepository siteRepository;
    private final SiteDomainRepository siteDomainRepository;

    public SiteDomainApplicationService(SiteRepository siteRepository, SiteDomainRepository siteDomainRepository) {
        this.siteRepository = siteRepository;
        this.siteDomainRepository = siteDomainRepository;
    }

    public SiteDomainDto bindDomain(BindSiteDomainCommand command) {
        Long tenantId = requireTenantId();
        Site site = siteRepository.findByIdAndTenantId(command.siteId(), tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));

        String normalizedDomain = normalizeDomain(command.domain());
        if (normalizedDomain.isBlank()) {
            throw new BusinessException("Domain is invalid");
        }

        siteDomainRepository.findByDomain(normalizedDomain).ifPresent(existing -> {
            throw new BusinessException("Domain already bound");
        });

        boolean primary = resolvePrimaryFlag(tenantId, site.getId(), command.primary());
        if (primary) {
            siteDomainRepository.clearPrimaryForSite(tenantId, site.getId());
        }

        SiteDomain siteDomain = SiteDomain.create(
                tenantId,
                site.getId(),
                normalizedDomain,
                primary,
                command.expiryAt()
        );
        return SiteDomainDto.from(siteDomainRepository.save(siteDomain));
    }

    public List<SiteDomainDto> listDomains(Long siteId) {
        Long tenantId = requireTenantId();
        siteRepository.findByIdAndTenantId(siteId, tenantId)
                .orElseThrow(() -> new BusinessException("Site not found"));
        return siteDomainRepository.findBySiteId(tenantId, siteId)
                .stream()
                .map(SiteDomainDto::from)
                .toList();
    }

    public void ensurePrimaryDomain(Long tenantId, Long siteId, String rawDomain) {
        String normalizedDomain = normalizeDomain(rawDomain);
        if (normalizedDomain.isBlank() || siteDomainRepository.existsByDomain(normalizedDomain)) {
            return;
        }

        boolean primary = siteDomainRepository.countBySiteId(tenantId, siteId) == 0;
        if (primary) {
            siteDomainRepository.clearPrimaryForSite(tenantId, siteId);
        }
        siteDomainRepository.save(SiteDomain.create(tenantId, siteId, normalizedDomain, primary, null));
    }

    public boolean existsDomain(String rawDomain) {
        String normalizedDomain = normalizeDomain(rawDomain);
        return !normalizedDomain.isBlank() && siteDomainRepository.existsByDomain(normalizedDomain);
    }

    private boolean resolvePrimaryFlag(Long tenantId, Long siteId, Boolean requestedPrimary) {
        if (siteDomainRepository.countBySiteId(tenantId, siteId) == 0) {
            return true;
        }
        return Boolean.TRUE.equals(requestedPrimary);
    }

    private Long requireTenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new BusinessException("Tenant context is missing");
        }
        return tenantId;
    }

    private String normalizeDomain(String value) {
        String candidate = value == null ? "" : value.trim().toLowerCase();
        if (candidate.isBlank()) {
            return "";
        }

        try {
            if (candidate.startsWith("http://") || candidate.startsWith("https://")) {
                URI uri = URI.create(candidate);
                return sanitizeHost(uri.getHost());
            }
            return sanitizeHost(URI.create("https://" + candidate).getHost());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Domain is invalid");
        }
    }

    private String sanitizeHost(String host) {
        if (host == null) {
            return "";
        }
        String sanitized = host.trim().toLowerCase();
        if (sanitized.endsWith(".")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        if (sanitized.isBlank() || sanitized.contains("/") || sanitized.contains(" ")) {
            return "";
        }
        return sanitized;
    }
}
