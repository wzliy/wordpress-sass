package com.wpss.wordpresssass.page.domain;

import java.util.List;
import java.util.Optional;

public interface PageLayoutVersionRepository {

    PageLayoutVersion save(PageLayoutVersion version);

    void update(PageLayoutVersion version);

    Optional<PageLayoutVersion> findByIdAndTenantId(Long id, Long tenantId);

    List<PageLayoutVersion> findByPage(Long tenantId, Long siteId, Long pageId);
}
