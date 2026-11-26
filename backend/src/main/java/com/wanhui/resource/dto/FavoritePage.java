package com.wanhui.resource.dto;

import java.util.List;

public record FavoritePage(
        List<FavoriteResourceResponse> items,
        int page,
        int size,
        long total,
        int totalPages
) {
    public FavoritePage {
        items = List.copyOf(items);
    }
}
