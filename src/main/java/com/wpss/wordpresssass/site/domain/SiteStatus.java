package com.wpss.wordpresssass.site.domain;

public enum SiteStatus {
    DISABLED(0),
    ENABLED(1);

    private final int code;

    SiteStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static SiteStatus fromCode(Integer code) {
        if (code == null || code == 1) {
            return ENABLED;
        }
        return DISABLED;
    }
}
