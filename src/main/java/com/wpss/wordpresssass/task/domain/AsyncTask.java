package com.wpss.wordpresssass.task.domain;

import java.time.LocalDateTime;

public class AsyncTask {

    private final Long id;
    private final Long tenantId;
    private final AsyncTaskType taskType;
    private final String bizType;
    private final Long bizId;
    private final String idempotencyKey;
    private final AsyncTaskStatus status;
    private final int priority;
    private final String payloadJson;
    private final String resultJson;
    private final String errorMessage;
    private final int retryCount;
    private final int maxRetryCount;
    private final LocalDateTime nextRunAt;
    private final String lockedBy;
    private final LocalDateTime lockedAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;
    private final Long createdBy;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AsyncTask(Long id, Long tenantId, AsyncTaskType taskType, String bizType, Long bizId, String idempotencyKey,
                     AsyncTaskStatus status, int priority, String payloadJson, String resultJson, String errorMessage,
                     int retryCount, int maxRetryCount, LocalDateTime nextRunAt, String lockedBy, LocalDateTime lockedAt,
                     LocalDateTime startedAt, LocalDateTime finishedAt, Long createdBy, LocalDateTime createdAt,
                     LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.taskType = taskType;
        this.bizType = bizType;
        this.bizId = bizId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.priority = priority;
        this.payloadJson = payloadJson;
        this.resultJson = resultJson;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetryCount = maxRetryCount;
        this.nextRunAt = nextRunAt;
        this.lockedBy = lockedBy;
        this.lockedAt = lockedAt;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AsyncTask createPending(Long tenantId, AsyncTaskType taskType, String bizType, Long bizId,
                                          String idempotencyKey, int priority, String payloadJson, int maxRetryCount,
                                          Long createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                null,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.PENDING,
                priority,
                payloadJson,
                null,
                null,
                0,
                maxRetryCount,
                now,
                null,
                null,
                null,
                null,
                createdBy,
                now,
                now
        );
    }

    public AsyncTask withId(Long newId) {
        return new AsyncTask(
                newId,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                status,
                priority,
                payloadJson,
                resultJson,
                errorMessage,
                retryCount,
                maxRetryCount,
                nextRunAt,
                lockedBy,
                lockedAt,
                startedAt,
                finishedAt,
                createdBy,
                createdAt,
                updatedAt
        );
    }

    public AsyncTask markRunning(String workerId) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                id,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.RUNNING,
                priority,
                payloadJson,
                resultJson,
                null,
                retryCount,
                maxRetryCount,
                nextRunAt,
                workerId,
                now,
                startedAt == null ? now : startedAt,
                null,
                createdBy,
                createdAt,
                now
        );
    }

    public AsyncTask markSuccess(String newResultJson) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                id,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.SUCCESS,
                priority,
                payloadJson,
                newResultJson,
                null,
                retryCount,
                maxRetryCount,
                null,
                lockedBy,
                lockedAt,
                startedAt == null ? now : startedAt,
                now,
                createdBy,
                createdAt,
                now
        );
    }

    public AsyncTask markRetry(String newErrorMessage, String newResultJson, int newRetryCount, LocalDateTime newNextRunAt) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                id,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.RETRY_WAIT,
                priority,
                payloadJson,
                newResultJson,
                newErrorMessage,
                newRetryCount,
                maxRetryCount,
                newNextRunAt,
                null,
                null,
                startedAt == null ? now : startedAt,
                null,
                createdBy,
                createdAt,
                now
        );
    }

    public AsyncTask markFailed(String newErrorMessage, String newResultJson, int newRetryCount) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                id,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.FAILED,
                priority,
                payloadJson,
                newResultJson,
                newErrorMessage,
                newRetryCount,
                maxRetryCount,
                null,
                null,
                null,
                startedAt == null ? now : startedAt,
                now,
                createdBy,
                createdAt,
                now
        );
    }

    public AsyncTask cancel(String reason) {
        LocalDateTime now = LocalDateTime.now();
        return new AsyncTask(
                id,
                tenantId,
                taskType,
                bizType,
                bizId,
                idempotencyKey,
                AsyncTaskStatus.CANCELED,
                priority,
                payloadJson,
                resultJson,
                reason,
                retryCount,
                maxRetryCount,
                null,
                null,
                null,
                startedAt,
                finishedAt == null ? now : finishedAt,
                createdBy,
                createdAt,
                now
        );
    }

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public AsyncTaskType getTaskType() {
        return taskType;
    }

    public String getBizType() {
        return bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public AsyncTaskStatus getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public String getResultJson() {
        return resultJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public LocalDateTime getNextRunAt() {
        return nextRunAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
