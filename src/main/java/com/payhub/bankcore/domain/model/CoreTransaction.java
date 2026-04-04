package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.enums.TxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CoreTransaction(
        String coreTxnId,
        String requestId,
        String bizOrderId,
        String bizType,
        TxnType txnType,
        String customerNo,
        BigDecimal amount,
        String currency,
        String debitAccountNo,
        Long debitAccountSeqNo,
        String debitSubjectCode,
        String creditAccountNo,
        Long creditAccountSeqNo,
        String creditSubjectCode,
        CoreTransactionStatus status,
        String failureCode,
        String failureMessage,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
}
