package com.wpss.wordpresssass.page.domain;

import java.util.List;
import java.util.Optional;

public interface PageRepository {

    Page save(Page page);

    void update(Page page);

    Optional<Page> findBySiteAndPageKey(Long tenantId, Long siteId, String pageKey);

    List<Page> findBySite(Long tenantId, Long siteId);
}
