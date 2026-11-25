package com.wanhui.config;

import com.wanhui.common.api.ApiError;
import com.wanhui.common.api.ApiResponse;
import com.wanhui.common.trace.TraceContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class SecurityErrorWriter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void write(HttpServletResponse response, HttpStatus status, String code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.failure(new ApiError(code, message), TraceContext.currentOrNew())
        );
    }
}
