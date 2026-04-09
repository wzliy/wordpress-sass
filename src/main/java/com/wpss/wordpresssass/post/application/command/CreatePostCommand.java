package com.wpss.wordpresssass.post.application.command;

import jakarta.validation.constraints.NotBlank;

public record CreatePostCommand(
        @NotBlank(message = "must not be blank")
        String title,
        @NotBlank(message = "must not be blank")
        String content
) {
}
