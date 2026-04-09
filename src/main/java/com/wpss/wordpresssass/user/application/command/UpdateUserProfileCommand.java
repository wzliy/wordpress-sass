package com.wpss.wordpresssass.user.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileCommand(
        @NotNull(message = "must not be null")
        Long userId,
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a valid email")
        String email,
        @Size(max = 50, message = "size must be between 0 and 50")
        String nickname
) {
}
