package com.wanhui.resource.dto;

import java.time.LocalDateTime;

public record FavoriteResourceResponse(
        String resourceKey,
        String name,
        String description,
        String category,
        String subcategory,
        String url,
        String icon,
        boolean available,
        LocalDateTime favoritedAt
) {
}
