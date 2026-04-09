package com.wpss.wordpresssass.auth.domain;

import java.time.LocalDateTime;

public class UserAccount {

    private final Long id;
    private final Long tenantId;
    private final String username;
    private final String password;
    private final String email;
    private final String nickname;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;

    public UserAccount(Long id, Long tenantId, String username, String password, String email,
                       String nickname, UserRole role, UserStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
