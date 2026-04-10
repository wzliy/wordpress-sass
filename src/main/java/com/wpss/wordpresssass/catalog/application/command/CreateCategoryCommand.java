package com.wpss.wordpresssass.catalog.application.command;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryCommand(
        @NotBlank(message = "must not be blank")
        String name,
        String slug
) {
}
