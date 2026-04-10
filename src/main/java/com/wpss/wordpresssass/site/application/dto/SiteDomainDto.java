package com.wpss.wordpresssass.site.application.dto;

import com.wpss.wordpresssass.site.domain.SiteDomain;

import java.time.LocalDateTime;

public record SiteDomainDto(
        Long id,
        Long siteId,
        String domain,
        boolean primary,
        String status,
        LocalDateTime expiryAt,
        LocalDateTime createdAt
) {

    public static SiteDomainDto from(SiteDomain siteDomain) {
        return new SiteDomainDto(
                siteDomain.getId(),
                siteDomain.getSiteId(),
                siteDomain.getDomain(),
                siteDomain.isPrimary(),
                siteDomain.getStatus().name(),
                siteDomain.getExpiryAt(),
                siteDomain.getCreatedAt()
        );
    }
}
