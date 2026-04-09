package com.wpss.wordpresssass.user.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
        @NotBlank(message = "must not be blank")
        String username,
        @NotBlank(message = "must not be blank")
        String password,
        @NotBlank(message = "must not be blank")
        @Email(message = "must be a valid email")
        String email
) {
}
