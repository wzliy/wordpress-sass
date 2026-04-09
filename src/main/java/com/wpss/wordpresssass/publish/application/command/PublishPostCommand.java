package com.wpss.wordpresssass.publish.application.command;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PublishPostCommand(
        @NotNull(message = "must not be null")
        Long postId,
        @NotEmpty(message = "must not be empty")
        List<Long> siteIds
) {
}
