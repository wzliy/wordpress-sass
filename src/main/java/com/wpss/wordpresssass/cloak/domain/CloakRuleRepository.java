package com.wpss.wordpresssass.cloak.domain;

import java.util.List;
import java.util.Optional;

public interface CloakRuleRepository {

    CloakRule save(CloakRule cloakRule);

    void update(CloakRule cloakRule);

    Optional<CloakRule> findByIdAndTenantId(Long id, Long tenantId);

    List<CloakRule> findBySite(Long tenantId, Long siteId);
}
