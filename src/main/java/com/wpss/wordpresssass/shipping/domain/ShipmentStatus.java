package com.wpss.wordpresssass.shipping.domain;

public enum ShipmentStatus {
    NOT_SHIPPED,
    SHIPPED,
    DELIVERED,
    EXCEPTION;

    public static ShipmentStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NOT_SHIPPED;
        }
        return ShipmentStatus.valueOf(value.trim().toUpperCase());
    }
}
