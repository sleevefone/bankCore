package com.payhub.bankcore.application.dto;

import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.enums.TxnType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CoreTransactionResponse(
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
        boolean success,
        boolean retryable,
        String rawCode,
        String rawMessage,
        LocalDateTime occurredAt,
        LocalDateTime createdAt
) {
}
