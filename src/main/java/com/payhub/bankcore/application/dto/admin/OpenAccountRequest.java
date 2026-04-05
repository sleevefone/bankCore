package com.payhub.bankcore.application.dto.admin;

import com.payhub.bankcore.domain.enums.BalanceDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class OpenAccountRequest {
    @NotBlank
    private String customerNo;
    @NotBlank
    private String currency;
    @NotBlank
    private String subjectCode;
    @NotNull
    private BalanceDirection normalBalanceDirection;
    private BigDecimal interestRate;
}
