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
        dataObject.setEntityType(auditLog.getEntityType());
        dataObject.setEntityId(auditLog.getEntityId());
        dataObject.setOperationType(auditLog.getOperationType());
        dataObject.setOperatorId(auditLog.getOperatorId());
        dataObject.setTraceId(auditLog.getTraceId());
        dataObject.setBeforeSnapshot(auditLog.getBeforeSnapshot());
        dataObject.setAfterSnapshot(auditLog.getAfterSnapshot());
        dataObject.setCreatedAt(auditLog.getCreatedAt());
        coreAuditLogMapper.insert(dataObject);
    }
}
