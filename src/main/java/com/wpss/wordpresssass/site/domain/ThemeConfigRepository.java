package com.wpss.wordpresssass.site.domain;

public interface ThemeConfigRepository {

    void saveDefaultForSite(Site site, SiteTemplate siteTemplate);

    boolean existsBySite(Long tenantId, Long siteId);
}
