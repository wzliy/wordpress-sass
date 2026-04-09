package com.wpss.wordpresssass.site.infrastructure.wordpress;

public record WpConnectionResult(
        boolean success,
        String message
) {
}
