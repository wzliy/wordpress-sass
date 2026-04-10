package com.wpss.wordpresssass.site.domain;

import java.util.List;
import java.util.Optional;

public interface SiteTemplateRepository {

    SiteTemplate save(SiteTemplate siteTemplate);

    long countAll();

    List<SiteTemplate> findAvailableTemplates(Long tenantId);

    Optional<SiteTemplate> findAccessibleById(Long tenantId, Long templateId);

    Optional<SiteTemplate> findAccessibleByCode(Long tenantId, String code);
}
