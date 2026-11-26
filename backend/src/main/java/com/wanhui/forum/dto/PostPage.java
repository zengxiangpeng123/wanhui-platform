package com.wanhui.forum.dto;

import java.util.List;

public record PostPage(List<PostSummaryResponse> items, int page, int size, long total, int totalPages) {
    public PostPage {
        items = List.copyOf(items);
    }
}
