package com.wpss.wordpresssass.site.application.dto;

import java.util.List;

public record AdminSiteDetailDto(
        SiteDto site,
        List<SiteDomainDto> domains
) {
}
