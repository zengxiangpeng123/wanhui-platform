package com.wanhui.forum.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        String id,
        String title,
        String content,
        PostAuthorResponse author,
        PostCategoryResponse category,
        List<String> tags,
        long viewCount,
        long commentCount,
        long likeCount,
        long favoriteCount,
        boolean liked,
        boolean favorited,
        LocalDateTime createdAt
) {
    public PostDetailResponse {
        tags = List.copyOf(tags);
    }
}
