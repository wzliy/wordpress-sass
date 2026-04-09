package com.wpss.wordpresssass.site.infrastructure.provision;

import com.wpss.wordpresssass.site.domain.Site;

public interface SiteProvisioner {

    ProvisionedSite provision(Site site, ProvisionContext context);
}
