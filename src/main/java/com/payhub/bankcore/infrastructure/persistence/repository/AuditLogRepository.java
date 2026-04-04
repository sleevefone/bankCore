package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.common.JacksonMapper;
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
        CoreAuditLogDO dataObject = JacksonMapper.convertValue(auditLog, CoreAuditLogDO.class);
        coreAuditLogMapper.insert(dataObject);
    }
}
