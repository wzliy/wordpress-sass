package com.wpss.wordpresssass.order.domain;

public enum ShippingStatus {
    NOT_SHIPPED,
    SHIPPED,
    DELIVERED,
    EXCEPTION;

    public static ShippingStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return NOT_SHIPPED;
        }
        if ("PENDING".equalsIgnoreCase(value.trim())) {
            return NOT_SHIPPED;
        }
        return ShippingStatus.valueOf(value.trim().toUpperCase());
    }
}
