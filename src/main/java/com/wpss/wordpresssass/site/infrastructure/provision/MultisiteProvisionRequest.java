package com.wpss.wordpresssass.site.infrastructure.provision;

public record MultisiteProvisionRequest(
        String title,
        String slug,
        String adminEmail,
        Long tenantId
) {
}
