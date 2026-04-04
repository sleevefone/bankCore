package com.payhub.bankcore.application.dto;

import com.payhub.bankcore.domain.enums.TxnType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateCoreTransactionRequest {
    @NotBlank
    private String requestId;
    @NotBlank
    private String bizOrderId;
    @NotBlank
    private String bizType;
    @NotNull
    private TxnType txnType;
    @NotBlank
    private String customerNo;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    private String currency;
    @NotBlank
    private String debitAccountNo;
    @NotNull
    private Long debitAccountSeqNo;
    @NotBlank
    private String debitSubjectCode;
    @NotBlank
    private String creditAccountNo;
    @NotNull
    private Long creditAccountSeqNo;
    @NotBlank
    private String creditSubjectCode;
    @NotNull
    private LocalDateTime occurredAt;
    private String remark;
}
