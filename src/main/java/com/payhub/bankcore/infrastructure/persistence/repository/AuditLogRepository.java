package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.domain.model.AuditLog;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreAuditLogDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreAuditLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class AuditLogRepository {

    private final CoreAuditLogMapper coreAuditLogMapper;

    public AuditLogRepository(CoreAuditLogMapper coreAuditLogMapper) {
        this.coreAuditLogMapper = coreAuditLogMapper;
    }

    public void save(AuditLog auditLog) {
        CoreAuditLogDO dataObject = new CoreAuditLogDO();
        dataObject.setEntityType(auditLog.entityType());
        dataObject.setEntityId(auditLog.entityId());
        dataObject.setOperationType(auditLog.operationType());
        dataObject.setOperatorId(auditLog.operatorId());
        dataObject.setTraceId(auditLog.traceId());
        dataObject.setBeforeSnapshot(auditLog.beforeSnapshot());
        dataObject.setAfterSnapshot(auditLog.afterSnapshot());
        dataObject.setCreatedAt(auditLog.createdAt());
        coreAuditLogMapper.insert(dataObject);
    }
}
