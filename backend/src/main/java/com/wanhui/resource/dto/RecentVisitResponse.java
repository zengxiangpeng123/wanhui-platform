package com.wanhui.resource.dto;

import java.time.LocalDateTime;

public record RecentVisitResponse(
        String resourceKey,
        String resourceName,
        LocalDateTime visitedAt
) {
}
