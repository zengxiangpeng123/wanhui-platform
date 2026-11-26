package com.wanhui.resource.dto;

import jakarta.validation.constraints.NotBlank;

public record RecentVisitRequest(@NotBlank String resourceKey) {
}
