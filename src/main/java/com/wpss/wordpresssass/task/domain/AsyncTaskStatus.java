package com.wpss.wordpresssass.task.domain;

public enum AsyncTaskStatus {
    PENDING(false),
    RUNNING(false),
    RETRY_WAIT(false),
    SUCCESS(true),
    FAILED(true),
    CANCELED(true);

    private final boolean terminal;

    AsyncTaskStatus(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isTerminal() {
        return terminal;
    }
}
