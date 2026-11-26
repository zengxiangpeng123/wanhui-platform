package com.wanhui.resource;

import com.wanhui.common.exception.GlobalExceptionHandler;
import com.wanhui.common.trace.TraceIdFilter;
import com.wanhui.resource.catalog.ResourceCatalogLoader;
import com.wanhui.resource.controller.ResourceController;
import com.wanhui.resource.service.ResourceQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ResourceCatalogLoader loader = new ResourceCatalogLoader();
        ResourceQueryService service = new ResourceQueryService(loader);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ResourceController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new TraceIdFilter())
                .build();
    }

    @Test
    void filtersByCategoryAndReturnsPaginationMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/resources")
                        .param("category", "design")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(75))
                .andExpect(jsonPath("$.data.items.length()").value(20))
                .andExpect(jsonPath("$.data.items[0].category").value("design"));
    }

    @Test
    void searchesByNameAndDescription() throws Exception {
        mockMvc.perform(get("/api/v1/resources").param("q", "Figma"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.items[0].name").value(org.hamcrest.Matchers.containsString("Figma")));
    }

    @Test
    void returnsEmptyResultForUnknownKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/resources").param("q", "no-such-resource-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.items.length()").value(0));
    }

    @Test
    void returns404ForUnknownResourceKey() throws Exception {
        mockMvc.perform(get("/api/v1/resources/not-a-resource"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
    }
}
