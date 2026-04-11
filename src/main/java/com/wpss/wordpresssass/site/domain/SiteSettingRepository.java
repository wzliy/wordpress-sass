package com.wpss.wordpresssass.site.domain;

import java.util.Optional;

public interface SiteSettingRepository {

    void saveDefaultForSite(Site site, SiteTemplate siteTemplate);

    boolean existsBySite(Long tenantId, Long siteId);

    Optional<SiteSetting> findBySite(Long tenantId, Long siteId);

    void saveOrUpdateDefaultConfig(Long tenantId, Long siteId, String defaultConfigJson);
}
