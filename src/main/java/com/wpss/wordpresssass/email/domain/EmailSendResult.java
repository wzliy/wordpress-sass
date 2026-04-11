package com.wpss.wordpresssass.email.domain;

public record EmailSendResult(
        EmailRecordStatus status,
        String responseMessage
) {
}
