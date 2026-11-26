package com.wanhui.resource.controller;

import com.wanhui.common.api.ApiResponse;
import com.wanhui.resource.catalog.CatalogResource;
import com.wanhui.resource.service.CatalogPage;
import com.wanhui.resource.service.ResourceQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resources")
public class ResourceController {
    private final ResourceQueryService queryService;

    public ResourceController(ResourceQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ApiResponse<CatalogPage> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(queryService.query(category, q, page, size));
    }

    @GetMapping("/{resourceKey}")
    public ApiResponse<CatalogResource> detail(@PathVariable String resourceKey) {
        return ApiResponse.success(queryService.findByKey(resourceKey));
    }
}
