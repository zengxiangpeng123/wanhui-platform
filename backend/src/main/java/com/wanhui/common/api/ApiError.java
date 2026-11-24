package com.wanhui.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiError(
        String code,
        String message,
        List<FieldError> fieldErrors
) {
    public ApiError(String code, String message) {
        this(code, message, List.of());
    }

    public ApiError {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public record FieldError(String field, String message) {
    }
}
