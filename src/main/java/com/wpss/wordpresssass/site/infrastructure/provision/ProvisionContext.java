package com.wpss.wordpresssass.site.infrastructure.provision;

public record ProvisionContext(
        String subdomainPrefix,
        String adminEmail,
        String networkDomain
) {
}
