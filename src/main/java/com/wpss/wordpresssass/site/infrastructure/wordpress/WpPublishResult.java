package com.wpss.wordpresssass.site.infrastructure.wordpress;

public record WpPublishResult(
        boolean success,
        Integer httpStatus,
        Long remotePostId,
        String remotePostUrl,
        String responseBody,
        String message,
        boolean retryable
) {

    public static WpPublishResult success(Integer httpStatus, Long remotePostId, String remotePostUrl, String responseBody) {
        return new WpPublishResult(true, httpStatus, remotePostId, remotePostUrl, responseBody, "Publish successful", false);
    }

    public static WpPublishResult failure(Integer httpStatus, String message, boolean retryable) {
        return new WpPublishResult(false, httpStatus, null, null, message, message, retryable);
    }
}
