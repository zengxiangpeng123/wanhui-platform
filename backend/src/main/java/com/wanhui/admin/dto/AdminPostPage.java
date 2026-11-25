package com.wanhui.admin.dto;

import com.wanhui.forum.dto.PostSummaryResponse;
import java.util.List;

public record AdminPostPage(List<PostSummaryResponse> items, int page, int size, long total, int totalPages) {
    public AdminPostPage {
        items = List.copyOf(items);
    }
}
