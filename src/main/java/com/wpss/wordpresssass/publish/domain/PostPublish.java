package com.wpss.wordpresssass.publish.domain;

import java.time.LocalDateTime;

public class PostPublish {

    private final Long id;
    private final Long tenantId;
    private final Long postId;
    private final Long siteId;
    private final String idempotencyKey;
    private final PublishStatus publishStatus;
    private final String targetStatus;
    private final Integer lastHttpStatus;
    private final Long remotePostId;
    private final String remotePostUrl;
    private final String errorMessage;
    private final String responseBody;
    private final int retryCount;
    private final int maxRetryCount;
    private final LocalDateTime nextRetryAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final LocalDateTime createdAt;

    public PostPublish(Long id, Long tenantId, Long postId, Long siteId, String idempotencyKey,
                       PublishStatus publishStatus, String targetStatus, Integer lastHttpStatus,
                       Long remotePostId, String remotePostUrl, String errorMessage, String responseBody,
                       int retryCount, int maxRetryCount, LocalDateTime nextRetryAt,
                       LocalDateTime startedAt, LocalDateTime finishedAt, LocalDateTime createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.postId = postId;
        this.siteId = siteId;
        this.idempotencyKey = idempotencyKey;
        this.publishStatus = publishStatus;
        this.targetStatus = targetStatus;
        this.lastHttpStatus = lastHttpStatus;
        this.remotePostId = remotePostId;
        this.remotePostUrl = remotePostUrl;
        this.errorMessage = errorMessage;
        this.responseBody = responseBody;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.nextRetryAt = nextRetryAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
    }

    public static PostPublish pending(Long tenantId, Long postId, Long siteId, String targetStatus, int maxRetryCount) {
        LocalDateTime now = LocalDateTime.now();
        return new PostPublish(
                null,
                tenantId,
                postId,
                siteId,
                buildIdempotencyKey(tenantId, postId, siteId, targetStatus),
                PublishStatus.PENDING,
                targetStatus,
                null,
                null,
                null,
                null,
                null,
                0,
                maxRetryCount,
                null,
                null,
                null,
                now
        );
    }

    public static PostPublish success(Long tenantId, Long postId, Long siteId, String targetStatus,
                                      Integer httpStatus, Long remotePostId, String remotePostUrl, String responseBody,
                                      int retryCount, int maxRetryCount) {
        LocalDateTime now = LocalDateTime.now();
        return new PostPublish(
                null,
                tenantId,
                postId,
                siteId,
                buildIdempotencyKey(tenantId, postId, siteId, targetStatus),
                PublishStatus.SUCCESS,
                targetStatus,
                httpStatus,
                remotePostId,
                remotePostUrl,
                null,
                responseBody,
                retryCount,
                maxRetryCount,
                null,
                now,
                now,
                now
        );
    }

    public static PostPublish failed(Long tenantId, Long postId, Long siteId, String targetStatus,
                                     Integer httpStatus, String errorMessage, String responseBody,
                                     int retryCount, int maxRetryCount, boolean retryable) {
        LocalDateTime now = LocalDateTime.now();
        return new PostPublish(
                null,
                tenantId,
                postId,
                siteId,
                buildIdempotencyKey(tenantId, postId, siteId, targetStatus),
                retryable ? PublishStatus.RETRY_WAIT : PublishStatus.FAILED,
                targetStatus,
                httpStatus,
                null,
                null,
                errorMessage,
                responseBody,
                retryCount,
                maxRetryCount,
                retryable ? now.plusMinutes(1) : null,
                now,
                now,
                now
        );
    }

    private static String buildIdempotencyKey(Long tenantId, Long postId, Long siteId, String targetStatus) {
        return tenantId + ":" + postId + ":" + siteId + ":" + targetStatus;
    }

    public PostPublish withId(Long newId) {
        return new PostPublish(newId, tenantId, postId, siteId, idempotencyKey, publishStatus, targetStatus, lastHttpStatus,
                remotePostId, remotePostUrl, errorMessage, responseBody, retryCount, maxRetryCount, nextRetryAt,
                startedAt, finishedAt, createdAt);
    }

    public PostPublish markProcessing() {
        return new PostPublish(
                id,
                tenantId,
                postId,
                siteId,
                idempotencyKey,
                PublishStatus.PROCESSING,
                targetStatus,
                lastHttpStatus,
                remotePostId,
                remotePostUrl,
                null,
                responseBody,
                retryCount,
                maxRetryCount,
                null,
                LocalDateTime.now(),
                null,
                createdAt
        );
    }

    public PostPublish markSuccess(Integer httpStatus, Long newRemotePostId, String newRemotePostUrl,
                                   String newResponseBody, int newRetryCount) {
        LocalDateTime now = LocalDateTime.now();
        return new PostPublish(
                id,
                tenantId,
                postId,
                siteId,
                idempotencyKey,
                PublishStatus.SUCCESS,
                targetStatus,
                httpStatus,
                newRemotePostId,
                newRemotePostUrl,
                null,
                newResponseBody,
                newRetryCount,
                maxRetryCount,
                null,
                startedAt == null ? now : startedAt,
                now,
                createdAt
        );
    }

    public PostPublish markFailure(Integer httpStatus, String newErrorMessage, String newResponseBody, int newRetryCount) {
        LocalDateTime now = LocalDateTime.now();
        return new PostPublish(
                id,
                tenantId,
                postId,
                siteId,
                idempotencyKey,
                PublishStatus.FAILED,
                targetStatus,
                httpStatus,
                null,
                null,
                newErrorMessage,
                newResponseBody,
                newRetryCount,
                maxRetryCount,
                null,
                startedAt == null ? now : startedAt,
                now,
                createdAt
        );
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getPostId() { return postId; }
    public Long getSiteId() { return siteId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public PublishStatus getPublishStatus() { return publishStatus; }
    public String getTargetStatus() { return targetStatus; }
    public Integer getLastHttpStatus() { return lastHttpStatus; }
    public Long getRemotePostId() { return remotePostId; }
    public String getRemotePostUrl() { return remotePostUrl; }
    public String getErrorMessage() { return errorMessage; }
    public String getResponseBody() { return responseBody; }
    public int getRetryCount() { return retryCount; }
    public int getMaxRetryCount() { return maxRetryCount; }
    public LocalDateTime getNextRetryAt() { return nextRetryAt; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
