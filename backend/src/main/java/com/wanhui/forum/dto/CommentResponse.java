package com.wanhui.forum.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        String id,
        String content,
        PostAuthorResponse author,
        long likeCount,
        boolean liked,
        LocalDateTime createdAt
) {
}
