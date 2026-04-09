package com.wpss.wordpresssass.site.application.command;

import jakarta.validation.constraints.NotBlank;

public record AddSiteCommand(
        @NotBlank(message = "must not be blank")
        String name,
        @NotBlank(message = "must not be blank")
        String baseUrl,
        @NotBlank(message = "must not be blank")
        String wpUsername,
        @NotBlank(message = "must not be blank")
        String appPassword
) {
}
