package com.wpss.wordpresssass.site.domain;

import java.time.LocalDateTime;

public class SiteDomain {

    private final Long id;
    private final Long tenantId;
    private final Long siteId;
    private final String domain;
    private final boolean primary;
    private final SiteDomainStatus status;
    private final LocalDateTime expiryAt;
    private final LocalDateTime createdAt;

    public SiteDomain(Long id,
                      Long tenantId,
                      Long siteId,
                      String domain,
                      boolean primary,
                      SiteDomainStatus status,
                      LocalDateTime expiryAt,
                      LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.siteId = siteId;
        this.domain = domain;
        this.primary = primary;
        this.status = status;
        this.expiryAt = expiryAt;
        this.createdAt = createdAt;
    }

    public static SiteDomain create(Long tenantId, Long siteId, String domain, boolean primary, LocalDateTime expiryAt) {
        return new SiteDomain(
                null,
                tenantId,
                siteId,
                domain,
                primary,
                SiteDomainStatus.ACTIVE,
                expiryAt,
                LocalDateTime.now()
        );
    }

    public SiteDomain withId(Long newId) {
        return new SiteDomain(newId, tenantId, siteId, domain, primary, status, expiryAt, createdAt);
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public String getDomain() {
        return domain;
    }

    public boolean isPrimary() {
        return primary;
    }

    public SiteDomainStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpiryAt() {
        return expiryAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
