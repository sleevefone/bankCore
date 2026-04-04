package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.DcDirection;
import com.payhub.bankcore.domain.enums.EntryDirection;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {
    private String coreTxnId;
    private Integer entryNo;
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String subjectCode;
    private EntryDirection entryDirection;
    private DcDirection dcDirection;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
}
