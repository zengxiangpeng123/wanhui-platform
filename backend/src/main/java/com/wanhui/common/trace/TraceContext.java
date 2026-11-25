package com.wanhui.common.trace;

import org.slf4j.MDC;

import java.util.UUID;

public final class TraceContext {
    public static final String MDC_KEY = "traceId";
    public static final String REQUEST_HEADER = "X-Request-Id";
    public static final String RESPONSE_HEADER = "X-Trace-Id";

    private TraceContext() {
    }

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static String currentOrNew() {
        String traceId = current();
        return traceId == null || traceId.isBlank() ? newTraceId() : traceId;
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
