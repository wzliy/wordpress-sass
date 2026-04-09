package com.wpss.wordpresssass.common.auth;

public record CurrentUser(
        Long userId,
        Long tenantId,
        String username
) {
}
