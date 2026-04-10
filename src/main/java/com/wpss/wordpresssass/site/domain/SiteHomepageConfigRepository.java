package com.wpss.wordpresssass.site.domain;

import java.util.Optional;

public interface SiteHomepageConfigRepository {

    void saveDefaultForSite(Site site, SiteTemplate siteTemplate);

    boolean existsBySite(Long tenantId, Long siteId);

    Optional<SiteHomepageConfig> findBySite(Long tenantId, Long siteId);
}
