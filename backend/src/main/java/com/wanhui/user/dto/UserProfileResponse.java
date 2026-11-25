package com.wanhui.user.dto;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String id,
        String username,
        String nickname,
        String avatarKey,
        String emailMasked,
        String phoneMasked,
        String interest,
        String bio,
        LocalDateTime createdAt
) {
}
