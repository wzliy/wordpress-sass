package com.wpss.wordpresssass.storefront.application;

import com.wpss.wordpresssass.site.domain.Site;
import com.wpss.wordpresssass.site.domain.SiteDomainRepository;
import com.wpss.wordpresssass.site.domain.SiteRepository;
import com.wpss.wordpresssass.site.domain.SiteStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HostDomainResolver {

    private final SiteDomainRepository siteDomainRepository;
    private final SiteRepository siteRepository;

    public HostDomainResolver(SiteDomainRepository siteDomainRepository, SiteRepository siteRepository) {
        this.siteDomainRepository = siteDomainRepository;
        this.siteRepository = siteRepository;
    }

    public Optional<Site> resolve(String hostHeader) {
        String host = normalizeHost(hostHeader);
        if (host.isBlank()) {
            return Optional.empty();
        }

        return siteDomainRepository.findActiveByDomain(host)
                .flatMap(siteDomain -> siteRepository.findByIdAndTenantId(siteDomain.getSiteId(), siteDomain.getTenantId()))
                .filter(site -> site.getStatus() == SiteStatus.ENABLED);
    }

    private String normalizeHost(String hostHeader) {
        if (hostHeader == null) {
            return "";
        }
        String host = hostHeader.trim().toLowerCase();
        int colonIndex = host.indexOf(':');
        if (colonIndex >= 0) {
            host = host.substring(0, colonIndex);
        }
        if (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }
}
