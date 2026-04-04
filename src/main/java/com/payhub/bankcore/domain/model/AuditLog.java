package com.payhub.bankcore.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private String entityType;
    private String entityId;
    private String operationType;
    private String operatorId;
    private String traceId;
    private String beforeSnapshot;
    private String afterSnapshot;
    private LocalDateTime createdAt;
}
