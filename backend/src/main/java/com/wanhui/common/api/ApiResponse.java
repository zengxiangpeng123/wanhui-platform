package com.wanhui.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanhui.common.trace.TraceContext;
import tools.jackson.databind.node.NullNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        @JsonIgnore T data,
        Object meta,
        ApiError error,
        String traceId
) {
    public static <T> ApiResponse<T> success(T data) {
        return success(data, null, TraceContext.currentOrNew());
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return success(data, null, traceId);
    }

    public static <T> ApiResponse<T> success(T data, Object meta, String traceId) {
        return new ApiResponse<>(true, data, meta, null, traceId);
    }

    public static <T> ApiResponse<T> failure(ApiError error, String traceId) {
        return new ApiResponse<>(false, null, null, error, traceId);
    }

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object jsonData() {
        return success && data == null ? NullNode.getInstance() : data;
    }
}
