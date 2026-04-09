package com.wpss.wordpresssass.publish.domain;

public enum PublishStatus {
    PENDING,
    PROCESSING,
    RETRY_WAIT,
    SUCCESS,
    FAILED,
    CANCELED
}
