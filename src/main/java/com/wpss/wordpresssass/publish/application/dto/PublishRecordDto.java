package com.wpss.wordpresssass.publish.application.dto;

import java.time.LocalDateTime;

public record PublishRecordDto(
        Long publishId,
        Long postId,
        String postTitle,
        Long siteId,
        String siteName,
        String status,
        String targetStatus,
        Integer retryCount,
        Integer lastHttpStatus,
        String message,
        Long remotePostId,
        String remotePostUrl,
        LocalDateTime createdAt
) {
}
