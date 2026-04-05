package com.payhub.bankcore.application.dto.admin;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class AccountView {
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String accountType;
    private String subjectCode;
    private String normalBalanceDirection;
    private String currency;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal interestRate;
    private String status;
}
