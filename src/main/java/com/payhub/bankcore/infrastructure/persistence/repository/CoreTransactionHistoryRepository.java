package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreTransactionHistoryDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreTransactionHistoryMapper;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;

@Repository
public class CoreTransactionHistoryRepository {

    private final CoreTransactionHistoryMapper coreTransactionHistoryMapper;

    public CoreTransactionHistoryRepository(CoreTransactionHistoryMapper coreTransactionHistoryMapper) {
        this.coreTransactionHistoryMapper = coreTransactionHistoryMapper;
    }

    public void save(
            String coreTxnId,
            String beforeStatus,
            String afterStatus,
            String eventType,
            String message,
            LocalDateTime createdAt
    ) {
        CoreTransactionHistoryDO dataObject = new CoreTransactionHistoryDO();
        dataObject.setCoreTxnId(coreTxnId);
        dataObject.setBeforeStatus(beforeStatus);
        dataObject.setAfterStatus(afterStatus);
        dataObject.setEventType(eventType);
        dataObject.setMessage(message);
        dataObject.setCreatedAt(createdAt);
        coreTransactionHistoryMapper.insert(dataObject);
    }
}
