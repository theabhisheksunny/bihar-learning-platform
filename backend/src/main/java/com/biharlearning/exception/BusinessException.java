package com.biharlearning.exception;

import org.springframework.http.HttpStatus;

/** Thrown for domain rule violations (e.g. source disabled, robots not confirmed). */
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;

    public BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
