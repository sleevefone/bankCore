package com.payhub.bankcore.application.dto;

import com.payhub.bankcore.domain.enums.TxnType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateCoreTransactionRequest(
        @NotBlank String requestId,
        @NotBlank String bizOrderId,
        @NotBlank String bizType,
        @NotNull TxnType txnType,
        @NotBlank String customerNo,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String debitAccountNo,
        @NotNull Long debitAccountSeqNo,
        @NotBlank String debitSubjectCode,
        @NotBlank String creditAccountNo,
        @NotNull Long creditAccountSeqNo,
        @NotBlank String creditSubjectCode,
        @NotNull LocalDateTime occurredAt,
        String remark
) {
}
