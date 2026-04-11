package com.wpss.wordpresssass.site.domain;

import java.util.List;
import java.util.Optional;

public interface SiteRepository {

    Site save(Site site);

    List<Site> findByTenantId(Long tenantId);

    Optional<Site> findByIdAndTenantId(Long id, Long tenantId);

    void updateStatus(Long id, Long tenantId, SiteStatus status, String statusMessage);

    void updateProvisionResult(Site site);

    void updateSubsiteSettings(Long tenantId,
                               Long siteId,
                               String name,
                               String countryCode,
                               String languageCode,
                               String currencyCode,
                               String logoUrl,
                               String bannerTitle,
                               String bannerSubtitle);
}
