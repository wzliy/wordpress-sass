package com.wpss.wordpresssass.user.application.command;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(
        @NotBlank(message = "must not be blank")
        String currentPassword,
        @NotBlank(message = "must not be blank")
        String newPassword
) {
}
