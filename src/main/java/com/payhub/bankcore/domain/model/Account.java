package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.AccountStatus;
import com.payhub.bankcore.domain.enums.AccountType;
import com.payhub.bankcore.domain.enums.BalanceDirection;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private AccountType accountType;
    private String subjectCode;
    private BalanceDirection normalBalanceDirection;
    private String ownerId;
    private String currency;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal interestRate;
    private LocalDate lastAccrualDate;
    private AccountStatus status;
}
