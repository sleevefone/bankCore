package com.payhub.bankcore.domain.model;

import java.time.LocalDateTime;

public record AuditLog(
        String entityType,
        String entityId,
        String operationType,
        String operatorId,
        String traceId,
        String beforeSnapshot,
        String afterSnapshot,
        LocalDateTime createdAt
) {
}
