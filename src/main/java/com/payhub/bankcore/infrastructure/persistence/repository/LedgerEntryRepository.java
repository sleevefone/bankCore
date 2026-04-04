package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.common.JacksonMapper;
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
            CoreLedgerEntryDO dataObject = JacksonMapper.convertValue(entry, CoreLedgerEntryDO.class);
            dataObject.setCreatedAt(now);
            coreLedgerEntryMapper.insert(dataObject);
        }
    }
}
