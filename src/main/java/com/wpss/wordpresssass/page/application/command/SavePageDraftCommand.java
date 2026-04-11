package com.wpss.wordpresssass.page.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

public record SavePageDraftCommand(
        @NotNull(message = "must not be null")
        JsonNode layout,
        String versionNote
) {
}
