package com.wanhui.admin.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        String operatorId,
        String targetType,
        String targetId,
        String action,
        String result,
        LocalDateTime occurredAt
) {
}
