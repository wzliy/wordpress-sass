package com.wpss.wordpresssass.auth.service;

import com.wpss.wordpresssass.auth.domain.UserAccount;
import com.wpss.wordpresssass.auth.domain.UserRole;
import com.wpss.wordpresssass.auth.domain.UserStatus;
import com.wpss.wordpresssass.auth.infrastructure.dataobject.UserAccountDO;
import com.wpss.wordpresssass.auth.infrastructure.mapper.UserAccountMapper;
import com.wpss.wordpresssass.common.auth.CurrentUser;
import com.wpss.wordpresssass.common.auth.UserContext;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.common.exception.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthApplicationService(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder,
                                  AuthTokenService authTokenService) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public AuthResult login(String username, String password) {
        UserAccount userAccount = userAccountMapper.selectByUsername(username)
                .map(this::toDomain)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(password, userAccount.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (userAccount.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException("账号已被禁用");
        }

        CurrentUser currentUser = new CurrentUser(userAccount.getId(), userAccount.getTenantId(), userAccount.getUsername());
        String token = authTokenService.createToken(currentUser);
        long expiresAt = Instant.now().getEpochSecond() + authTokenService.expireSeconds();
        return new AuthResult(
                token,
                currentUser.userId(),
                currentUser.tenantId(),
                currentUser.username(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getRole().name(),
                expiresAt,
                authTokenService.expireSeconds()
        );
    }

    public AuthResult currentUser() {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new UnauthorizedException("未登录");
        }
        UserAccount userAccount = userAccountMapper.selectById(currentUser.userId())
                .map(this::toDomain)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return new AuthResult(
                null,
                currentUser.userId(),
                currentUser.tenantId(),
                currentUser.username(),
                userAccount.getEmail(),
                userAccount.getNickname(),
                userAccount.getRole().name(),
                null,
                authTokenService.expireSeconds()
        );
    }

    private UserAccount toDomain(UserAccountDO userAccountDO) {
        return new UserAccount(
                userAccountDO.getId(),
                userAccountDO.getTenantId(),
                userAccountDO.getUsername(),
                userAccountDO.getPassword(),
                userAccountDO.getEmail(),
                userAccountDO.getNickname(),
                UserRole.valueOf(userAccountDO.getRole()),
                UserStatus.valueOf(userAccountDO.getStatus()),
                userAccountDO.getCreatedAt()
        );
    }

    public record AuthResult(
            String token,
            Long userId,
            Long tenantId,
            String username,
            String email,
            String nickname,
            String role,
            Long expiresAt,
            Long expireSeconds
    ) {
    }
}
