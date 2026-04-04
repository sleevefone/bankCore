package com.payhub.bankcore.application.dto;

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
public class CoreTransactionResponse {
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
    private boolean success;
    private boolean retryable;
    private String rawCode;
    private String rawMessage;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
