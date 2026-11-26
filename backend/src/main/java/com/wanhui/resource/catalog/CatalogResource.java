package com.wanhui.resource.catalog;

import java.util.List;

public record CatalogResource(
        String key,
        String name,
        String description,
        String category,
        String subcategory,
        String url,
        String icon,
        List<String> tags,
        Boolean favorited
) {
    public CatalogResource {
        tags = tags == null ? List.of() : List.copyOf(tags);
        favorited = favorited != null && favorited;
    }
}
