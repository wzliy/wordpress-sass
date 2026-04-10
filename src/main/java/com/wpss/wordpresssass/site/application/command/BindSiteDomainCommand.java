package com.wpss.wordpresssass.site.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BindSiteDomainCommand(
        @NotNull(message = "must not be null")
        Long siteId,
        @NotBlank(message = "must not be blank")
        String domain,
        Boolean primary,
        LocalDateTime expiryAt
) {
}
