package com.biharlearning.exception;

/** Thrown when a requested entity does not exist. Carries a stable error code. */
public class NotFoundException extends RuntimeException {
    private final String code;

    public NotFoundException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() { return code; }
}
