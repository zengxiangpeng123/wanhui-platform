package com.wanhui.forum.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostSummaryResponse(
        String id,
        String title,
        String excerpt,
        PostAuthorResponse author,
        PostCategoryResponse category,
        List<String> tags,
        long viewCount,
        long commentCount,
        long likeCount,
        long favoriteCount,
        LocalDateTime createdAt
) {
    public PostSummaryResponse {
        tags = List.copyOf(tags);
    }
}
