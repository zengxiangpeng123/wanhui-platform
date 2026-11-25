package com.wanhui.common.exception;

import com.wanhui.common.api.ApiError;
import org.springframework.http.HttpStatus;

import java.util.List;

public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final String code;
    private final List<ApiError.FieldError> fieldErrors;

    public BusinessException(HttpStatus status, String code, String message) {
        this(status, code, message, List.of());
    }

    public BusinessException(
            HttpStatus status,
            String code,
            String message,
            List<ApiError.FieldError> fieldErrors
    ) {
        super(message);
        this.status = status;
        this.code = code;
        this.fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public List<ApiError.FieldError> fieldErrors() {
        return fieldErrors;
    }
}
