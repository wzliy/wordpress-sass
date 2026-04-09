package com.wpss.wordpresssass.user.application;

import com.wpss.wordpresssass.auth.domain.UserAccount;
import com.wpss.wordpresssass.auth.domain.UserRole;
import com.wpss.wordpresssass.auth.domain.UserStatus;
import com.wpss.wordpresssass.auth.infrastructure.dataobject.UserAccountDO;
import com.wpss.wordpresssass.auth.infrastructure.mapper.UserAccountMapper;
import com.wpss.wordpresssass.common.auth.CurrentUser;
import com.wpss.wordpresssass.common.auth.UserContext;
import com.wpss.wordpresssass.common.exception.BusinessException;
import com.wpss.wordpresssass.user.application.command.ChangePasswordCommand;
import com.wpss.wordpresssass.user.application.command.CreateUserCommand;
import com.wpss.wordpresssass.user.application.command.ToggleUserStatusCommand;
import com.wpss.wordpresssass.user.application.command.UpdateUserProfileCommand;
import com.wpss.wordpresssass.user.application.dto.UserDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserManagementApplicationService {

    private final UserAccountMapper userAccountMapper;
    private final PasswordEncoder passwordEncoder;

    public UserManagementApplicationService(UserAccountMapper userAccountMapper, PasswordEncoder passwordEncoder) {
        this.userAccountMapper = userAccountMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> listUsers() {
        CurrentUser currentUser = requireCurrentUser();
        return userAccountMapper.selectByTenantId(currentUser.tenantId())
                .stream()
                .map(this::toDomain)
                .map(UserDto::from)
                .toList();
    }

    public UserDto createUser(CreateUserCommand command) {
        CurrentUser currentUser = requireCurrentUser();
        userAccountMapper.selectByUsername(command.username().trim())
                .ifPresent(existing -> {
                    throw new BusinessException("用户名已存在");
                });

        UserAccountDO userAccountDO = new UserAccountDO();
        userAccountDO.setTenantId(currentUser.tenantId());
        userAccountDO.setUsername(command.username().trim());
        userAccountDO.setPassword(passwordEncoder.encode(command.password()));
        userAccountDO.setEmail(command.email().trim());
        userAccountDO.setNickname(command.username().trim());
        userAccountDO.setRole(UserRole.ADMIN.name());
        userAccountDO.setStatus(UserStatus.ACTIVE.name());
        userAccountDO.setCreatedAt(LocalDateTime.now());
        userAccountMapper.insert(userAccountDO);
        return UserDto.from(toDomain(userAccountDO));
    }

    public void changePassword(ChangePasswordCommand command) {
        CurrentUser currentUser = requireCurrentUser();
        UserAccount userAccount = userAccountMapper.selectById(currentUser.userId())
                .map(this::toDomain)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (!passwordEncoder.matches(command.currentPassword(), userAccount.getPassword())) {
            throw new BusinessException("当前密码错误");
        }

        userAccountMapper.updatePassword(userAccount.getId(), currentUser.tenantId(), passwordEncoder.encode(command.newPassword()));
    }

    public void disableUser(ToggleUserStatusCommand command) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.userId().equals(command.userId())) {
            throw new BusinessException("不能禁用当前登录账号");
        }

        UserAccount userAccount = loadTenantUser(command.userId(), currentUser.tenantId());
        if (userAccount.getStatus() == UserStatus.DISABLED) {
            return;
        }

        userAccountMapper.updateStatus(userAccount.getId(), currentUser.tenantId(), UserStatus.DISABLED.name());
    }

    public void enableUser(ToggleUserStatusCommand command) {
        CurrentUser currentUser = requireCurrentUser();
        UserAccount userAccount = loadTenantUser(command.userId(), currentUser.tenantId());
        if (userAccount.getStatus() == UserStatus.ACTIVE) {
            return;
        }

        userAccountMapper.updateStatus(userAccount.getId(), currentUser.tenantId(), UserStatus.ACTIVE.name());
    }

    public UserDto getUserDetail(Long userId) {
        CurrentUser currentUser = requireCurrentUser();
        return UserDto.from(loadTenantUser(userId, currentUser.tenantId()));
    }

    public UserDto updateUserProfile(UpdateUserProfileCommand command) {
        CurrentUser currentUser = requireCurrentUser();
        UserAccount userAccount = loadTenantUser(command.userId(), currentUser.tenantId());
        String nickname = normalizeNickname(command.nickname(), userAccount.getUsername());
        userAccountMapper.updateProfile(userAccount.getId(), currentUser.tenantId(), command.email().trim(), nickname);
        return UserDto.from(loadTenantUser(userAccount.getId(), currentUser.tenantId()));
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser currentUser = UserContext.get();
        if (currentUser == null) {
            throw new BusinessException("未登录");
        }
        return currentUser;
    }

    private UserAccount loadTenantUser(Long userId, Long tenantId) {
        UserAccount userAccount = userAccountMapper.selectById(userId)
                .map(this::toDomain)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (!tenantId.equals(userAccount.getTenantId())) {
            throw new BusinessException("用户不存在");
        }
        return userAccount;
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

    private String normalizeNickname(String nickname, String fallbackUsername) {
        if (nickname == null || nickname.isBlank()) {
            return fallbackUsername;
        }
        return nickname.trim();
    }
}
