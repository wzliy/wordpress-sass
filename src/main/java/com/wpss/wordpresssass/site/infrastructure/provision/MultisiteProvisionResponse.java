package com.wpss.wordpresssass.site.infrastructure.provision;

public record MultisiteProvisionResponse(
        String baseUrl,
        String domain,
        String adminUrl,
        String wpUsername,
        String appPassword,
        String message
) {
}
