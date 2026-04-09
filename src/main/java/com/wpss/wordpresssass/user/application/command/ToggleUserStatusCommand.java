package com.wpss.wordpresssass.user.application.command;

import jakarta.validation.constraints.NotNull;

public record ToggleUserStatusCommand(
        @NotNull(message = "must not be null")
        Long userId
) {
}
