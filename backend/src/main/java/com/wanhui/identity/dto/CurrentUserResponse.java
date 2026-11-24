package com.wanhui.identity.dto;

import java.util.List;

public record CurrentUserResponse(
        String id,
        String username,
        String nickname,
        String avatarKey,
        String emailMasked,
        String phoneMasked,
        List<String> roles,
        List<String> permissions
) {
}
