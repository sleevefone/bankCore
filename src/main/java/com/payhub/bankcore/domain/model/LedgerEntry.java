package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.DcDirection;
import com.payhub.bankcore.domain.enums.EntryDirection;
import java.math.BigDecimal;

public record LedgerEntry(
        String coreTxnId,
        Integer entryNo,
        String accountNo,
        Long accountSeqNo,
        String customerNo,
        String subjectCode,
        EntryDirection entryDirection,
        DcDirection dcDirection,
        BigDecimal amount,
        String currency,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter
) {
}
