package com.wanhui.common.api;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void successResponseContainsDataAndTraceIdWithoutNullErrorFields() throws Exception {
        ApiResponse<Map<String, String>> response = ApiResponse.success(
                Map.of("id", "42"),
                "trace-success"
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("success").asBoolean()).isTrue();
        assertThat(json.get("data").get("id").asText()).isEqualTo("42");
        assertThat(json.get("traceId").asText()).isEqualTo("trace-success");
        assertThat(json.has("error")).isFalse();
        assertThat(json.has("meta")).isFalse();
    }

    @Test
    void successResponseKeepsNullDataForEmptyResultContracts() throws Exception {
        ApiResponse<Void> response = ApiResponse.success(null, "trace-empty");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("success").asBoolean()).isTrue();
        assertThat(json.has("data")).isTrue();
        assertThat(json.get("data").isNull()).isTrue();
        assertThat(json.get("traceId").asText()).isEqualTo("trace-empty");
    }

    @Test
    void failureResponseContainsStructuredErrorAndTraceId() throws Exception {
        ApiError error = new ApiError(
                "VALIDATION_FAILED",
                "请求参数校验失败",
                List.of(new ApiError.FieldError("title", "标题不能为空"))
        );

        ApiResponse<Void> response = ApiResponse.failure(error, "trace-failure");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("success").asBoolean()).isFalse();
        assertThat(json.get("error").get("code").asText()).isEqualTo("VALIDATION_FAILED");
        assertThat(json.get("error").get("fieldErrors").get(0).get("field").asText()).isEqualTo("title");
        assertThat(json.get("traceId").asText()).isEqualTo("trace-failure");
        assertThat(json.has("data")).isFalse();
    }
}
