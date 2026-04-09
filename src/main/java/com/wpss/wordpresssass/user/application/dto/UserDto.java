package com.wpss.wordpresssass.user.application.dto;

import com.wpss.wordpresssass.auth.domain.UserAccount;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        Long tenantId,
        String username,
        String email,
        String nickname,
        String role,
        String status,
        LocalDateTime createdAt
) {

    public static UserDto from(UserAccount userAccount) {
        return new UserDto(
                userAccount.getId(),
                userAccount.getTenantId(),
                userAccount.getUsername(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getRole().name(),
                userAccount.getStatus().name(),
                userAccount.getCreatedAt()
        );
    }
}
