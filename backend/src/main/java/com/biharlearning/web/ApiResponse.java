package com.biharlearning.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Consistent success/error envelope per docs/api.md. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, Map<String, Object> meta, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, Map<String, Object> meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static ApiResponse<Object> fail(String code, String message) {
        return new ApiResponse<>(false, null, null, new ApiError(code, message));
    }

    public record ApiError(String code, String message) {}
}
