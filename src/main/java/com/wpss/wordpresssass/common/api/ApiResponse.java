package com.wpss.wordpresssass.common.api;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "OK");
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, "OK");
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, null, message);
    }
}
