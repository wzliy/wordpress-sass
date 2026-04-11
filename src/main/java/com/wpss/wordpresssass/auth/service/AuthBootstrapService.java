package com.wpss.wordpresssass.auth.service;

import com.wpss.wordpresssass.auth.config.BootstrapAdminProperties;
import com.wpss.wordpresssass.auth.infrastructure.dataobject.TenantDO;
import com.wpss.wordpresssass.auth.infrastructure.dataobject.UserAccountDO;
import com.wpss.wordpresssass.auth.infrastructure.mapper.TenantMapper;
import com.wpss.wordpresssass.auth.infrastructure.mapper.UserAccountMapper;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Order(10)
public class AuthBootstrapService implements ApplicationRunner {

    private final TenantMapper tenantMapper;
    private final UserAccountMapper userAccountMapper;
    private final BootstrapAdminProperties bootstrapAdminProperties;
    private final PasswordEncoder passwordEncoder;

    public AuthBootstrapService(TenantMapper tenantMapper, UserAccountMapper userAccountMapper,
                                BootstrapAdminProperties bootstrapAdminProperties, PasswordEncoder passwordEncoder) {
        this.tenantMapper = tenantMapper;
        this.userAccountMapper = userAccountMapper;
        this.bootstrapAdminProperties = bootstrapAdminProperties;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (tenantMapper.countAll() > 0 || userAccountMapper.countAll() > 0) {
            return;
        }

        TenantDO tenantDO = new TenantDO();
        tenantDO.setName(bootstrapAdminProperties.getTenantName());
        tenantMapper.insert(tenantDO);

        UserAccountDO userAccountDO = new UserAccountDO();
        userAccountDO.setTenantId(tenantDO.getId());
        userAccountDO.setUsername(bootstrapAdminProperties.getAdminUsername());
        userAccountDO.setPassword(passwordEncoder.encode(bootstrapAdminProperties.getAdminPassword()));
        userAccountDO.setEmail(bootstrapAdminProperties.getAdminEmail());
        userAccountDO.setNickname(bootstrapAdminProperties.getAdminUsername());
        userAccountDO.setRole("ADMIN");
        userAccountDO.setStatus("ACTIVE");
        userAccountDO.setCreatedAt(LocalDateTime.now());
        userAccountMapper.insert(userAccountDO);
    }
}
