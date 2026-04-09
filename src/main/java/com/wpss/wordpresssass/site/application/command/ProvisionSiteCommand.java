package com.wpss.wordpresssass.site.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProvisionSiteCommand(
        @NotBlank(message = "must not be blank")
        String name,
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a valid email")
        String adminEmail,
        @NotBlank(message = "must not be blank")
        String subdomainPrefix
) {
}
