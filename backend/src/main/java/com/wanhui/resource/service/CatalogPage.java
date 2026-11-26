package com.wanhui.resource.service;

import com.wanhui.resource.catalog.CatalogResource;

import java.util.List;

public record CatalogPage(
        List<CatalogResource> items,
        int page,
        int size,
        long total,
        int totalPages,
        String sort
) {
    public CatalogPage {
        items = List.copyOf(items);
    }
}
