package com.wpss.wordpresssass.common.auth;

import com.wpss.wordpresssass.auth.domain.UserStatus;
import com.wpss.wordpresssass.auth.infrastructure.mapper.UserAccountMapper;
import com.wpss.wordpresssass.auth.service.AuthTokenService;
import com.wpss.wordpresssass.common.exception.UnauthorizedException;
import com.wpss.wordpresssass.common.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenService authTokenService;
    private final UserAccountMapper userAccountMapper;

    public AuthInterceptor(AuthTokenService authTokenService, UserAccountMapper userAccountMapper) {
        this.authTokenService = authTokenService;
        this.userAccountMapper = userAccountMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing Authorization header");
        }

        String token = authorization.substring(7);
        CurrentUser currentUser = authTokenService.parseToken(token);
        userAccountMapper.selectById(currentUser.userId())
                .ifPresentOrElse(userAccountDO -> {
                    if (!currentUser.tenantId().equals(userAccountDO.getTenantId())) {
                        throw new UnauthorizedException("Invalid token");
                    }
                    if (UserStatus.valueOf(userAccountDO.getStatus()) == UserStatus.DISABLED) {
                        throw new UnauthorizedException("账号已被禁用");
                    }
                }, () -> {
                    throw new UnauthorizedException("用户不存在");
                });
        UserContext.set(currentUser);
        TenantContext.setTenantId(currentUser.tenantId());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        TenantContext.clear();
    }
}
