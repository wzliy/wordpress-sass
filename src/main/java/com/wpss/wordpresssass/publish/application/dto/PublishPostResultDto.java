package com.wpss.wordpresssass.publish.application.dto;

import java.util.List;

public record PublishPostResultDto(
        Long postId,
        int totalSites,
        List<PublishSiteResultDto> results
) {
}
