package com.wpss.wordpresssass.site.domain;

import java.util.List;
import java.util.Optional;

public interface SiteDomainRepository {

    SiteDomain save(SiteDomain siteDomain);

    List<SiteDomain> findBySiteId(Long tenantId, Long siteId);

    Optional<SiteDomain> findByDomain(String domain);

    Optional<SiteDomain> findActiveByDomain(String domain);

    boolean existsByDomain(String domain);

    int countBySiteId(Long tenantId, Long siteId);

    void clearPrimaryForSite(Long tenantId, Long siteId);
}
