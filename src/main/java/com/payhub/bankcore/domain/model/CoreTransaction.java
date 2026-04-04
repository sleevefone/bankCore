package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.enums.TxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreTransaction {
    private String coreTxnId;
    private String requestId;
    private String bizOrderId;
    private String bizType;
    private TxnType txnType;
    private String customerNo;
    private BigDecimal amount;
    private String currency;
    private String debitAccountNo;
    private Long debitAccountSeqNo;
    private String debitSubjectCode;
    private String creditAccountNo;
    private Long creditAccountSeqNo;
    private String creditSubjectCode;
    private CoreTransactionStatus status;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
