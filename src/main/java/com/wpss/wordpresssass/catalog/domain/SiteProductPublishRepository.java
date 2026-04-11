package com.wpss.wordpresssass.catalog.domain;

import java.util.List;
import java.util.Optional;

public interface SiteProductPublishRepository {

    SiteProductPublish save(SiteProductPublish siteProductPublish);

    Optional<SiteProductPublish> findBySiteIdAndProductId(Long tenantId, Long siteId, Long productId);

    List<SiteProductPublish> findByProductId(Long tenantId, Long productId);

    void updateStatus(Long id, Long tenantId, SiteProductPublishStatus status);
}
