package com.wpss.wordpresssass.site.infrastructure.provision;

import com.wpss.wordpresssass.site.domain.Site;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "wordpress.multisite", name = "enabled", havingValue = "false", matchIfMissing = true)
public class MockMultisiteProvisioner implements SiteProvisioner {

    @Override
    public ProvisionedSite provision(Site site, ProvisionContext context) {
        String slug = slugify(context.subdomainPrefix());
        String domain = "https://" + slug + "." + context.networkDomain();
        return new ProvisionedSite(
                domain,
                domain,
                domain + "/wp-admin",
                "admin_" + site.getTenantId(),
                "app-" + slug + "-" + site.getTenantId(),
                "Mock multisite provision completed"
        );
    }

    private String slugify(String value) {
        return value.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
