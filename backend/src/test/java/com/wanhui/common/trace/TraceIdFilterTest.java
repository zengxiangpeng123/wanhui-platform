package com.wanhui.common.trace;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TraceIdFilterTest {

    @Test
    void usesClientRequestIdAndClearsMdcAfterRequest() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new TraceController())
                .addFilters(new TraceIdFilter())
                .build();

        mockMvc.perform(get("/test/trace").header(TraceIdFilter.REQUEST_HEADER, "client-trace-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(TraceIdFilter.RESPONSE_HEADER, "client-trace-123"))
                .andExpect(header().exists(TraceIdFilter.RESPONSE_HEADER));

        org.assertj.core.api.Assertions.assertThat(MDC.get(TraceIdFilter.MDC_KEY)).isNull();
    }

    @RestController
    static class TraceController {
        @GetMapping("/test/trace")
        String trace() {
            return "ok";
        }
    }
}
