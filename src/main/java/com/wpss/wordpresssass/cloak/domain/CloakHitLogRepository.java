package com.wpss.wordpresssass.cloak.domain;

import java.util.List;

public interface CloakHitLogRepository {

    CloakHitLog save(CloakHitLog cloakHitLog);

    List<CloakHitLog> findBySite(Long tenantId, Long siteId, int limit);
}
