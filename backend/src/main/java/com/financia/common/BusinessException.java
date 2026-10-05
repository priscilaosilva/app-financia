package com.financia.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final String field;

    public BusinessException(String message) {
        this(message, null);
    }

    public BusinessException(String message, String field) {
        super(message);
        this.field = field;
    }
}
