package com.wanhui.forum.dto;

import java.util.List;

public record CommentPage(List<CommentResponse> items, int page, int size, long total, int totalPages) {
    public CommentPage {
        items = List.copyOf(items);
    }
}
