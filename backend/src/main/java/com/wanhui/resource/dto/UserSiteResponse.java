package com.wanhui.resource.dto;

import java.time.LocalDateTime;

public record UserSiteResponse(
        String id,
        String name,
        String url,
        String iconUrl,
        LocalDateTime createdAt
) {
}
