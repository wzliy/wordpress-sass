package com.wpss.wordpresssass.publish.application.dto;

import com.wpss.wordpresssass.publish.domain.PostPublish;

public record PublishSiteResultDto(
        Long publishId,
        Long siteId,
        String status,
        String message,
        Integer retryCount,
        Long remotePostId,
        String remotePostUrl
) {

    public static PublishSiteResultDto from(PostPublish postPublish) {
        String message = switch (postPublish.getPublishStatus()) {
            case PENDING -> "Queued for execution";
            case PROCESSING -> "Publishing in progress";
            case SUCCESS -> "Publish successful";
            case RETRY_WAIT, FAILED -> postPublish.getErrorMessage() == null ? "Publish failed" : postPublish.getErrorMessage();
            case CANCELED -> "Publish canceled";
        };
        return new PublishSiteResultDto(
                postPublish.getId(),
                postPublish.getSiteId(),
                postPublish.getPublishStatus().name(),
                message,
                postPublish.getRetryCount(),
                postPublish.getRemotePostId(),
                postPublish.getRemotePostUrl()
        );
    }
}
