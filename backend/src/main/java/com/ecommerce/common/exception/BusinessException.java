package com.ecommerce.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }

    public static BusinessException notFound(String entity) {
        return new BusinessException(404, String.format("Not Found: %s", entity));
    }

    public static BusinessException unauthorized() {
        return new BusinessException(401, "Invalid email or password");
    }

    public static BusinessException duplicateEmail() {
        return new BusinessException(409, "Email already registered");
    }

    public static BusinessException insufficientStock() {
        return new BusinessException(400, "Insufficient stock");
    }
}
