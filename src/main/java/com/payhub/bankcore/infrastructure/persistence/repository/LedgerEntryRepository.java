package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.domain.model.LedgerEntry;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreLedgerEntryDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreLedgerEntryMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class LedgerEntryRepository {

    private final CoreLedgerEntryMapper coreLedgerEntryMapper;

    public LedgerEntryRepository(CoreLedgerEntryMapper coreLedgerEntryMapper) {
        this.coreLedgerEntryMapper = coreLedgerEntryMapper;
    }

    public void saveAll(List<LedgerEntry> entries) {
        LocalDateTime now = LocalDateTime.now();
        for (LedgerEntry entry : entries) {
            CoreLedgerEntryDO dataObject = new CoreLedgerEntryDO();
            dataObject.setCoreTxnId(entry.getCoreTxnId());
            dataObject.setEntryNo(entry.getEntryNo());
            dataObject.setAccountNo(entry.getAccountNo());
            dataObject.setAccountSeqNo(entry.getAccountSeqNo());
            dataObject.setCustomerNo(entry.getCustomerNo());
            dataObject.setSubjectCode(entry.getSubjectCode());
            dataObject.setEntryDirection(entry.getEntryDirection().name());
            dataObject.setDcDirection(entry.getDcDirection().name());
            dataObject.setAmount(entry.getAmount());
            dataObject.setCurrency(entry.getCurrency());
            dataObject.setBalanceBefore(entry.getBalanceBefore());
            dataObject.setBalanceAfter(entry.getBalanceAfter());
            dataObject.setCreatedAt(now);
            coreLedgerEntryMapper.insert(dataObject);
        }
    }
}
