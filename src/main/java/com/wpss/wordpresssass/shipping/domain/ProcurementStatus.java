package com.wpss.wordpresssass.shipping.domain;

public enum ProcurementStatus {
    NOT_ORDERED,
    ORDERED,
    FAILED;

    public static ProcurementStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NOT_ORDERED;
        }
        return ProcurementStatus.valueOf(value.trim().toUpperCase());
    }
}
