package com.wpss.wordpresssass.site.infrastructure.provision;

public record ProvisionedSite(
        String baseUrl,
        String domain,
        String adminUrl,
        String wpUsername,
        String appPassword,
        String message
) {
}
