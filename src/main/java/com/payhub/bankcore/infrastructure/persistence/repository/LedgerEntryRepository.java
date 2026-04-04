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
            dataObject.setCoreTxnId(entry.coreTxnId());
            dataObject.setEntryNo(entry.entryNo());
            dataObject.setAccountNo(entry.accountNo());
            dataObject.setAccountSeqNo(entry.accountSeqNo());
            dataObject.setCustomerNo(entry.customerNo());
            dataObject.setSubjectCode(entry.subjectCode());
            dataObject.setEntryDirection(entry.entryDirection().name());
            dataObject.setDcDirection(entry.dcDirection().name());
            dataObject.setAmount(entry.amount());
            dataObject.setCurrency(entry.currency());
            dataObject.setBalanceBefore(entry.balanceBefore());
            dataObject.setBalanceAfter(entry.balanceAfter());
            dataObject.setCreatedAt(now);
            coreLedgerEntryMapper.insert(dataObject);
        }
    }
}
