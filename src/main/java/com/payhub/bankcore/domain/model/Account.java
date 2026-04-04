package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.AccountStatus;
import com.payhub.bankcore.domain.enums.AccountType;
import com.payhub.bankcore.domain.enums.BalanceDirection;
import java.math.BigDecimal;
import java.time.LocalDate;

public record Account(
        String accountNo,
        Long accountSeqNo,
        String customerNo,
        AccountType accountType,
        String subjectCode,
        BalanceDirection normalBalanceDirection,
        String ownerId,
        String currency,
        BigDecimal availableBalance,
        BigDecimal frozenBalance,
        BigDecimal interestRate,
        LocalDate lastAccrualDate,
        AccountStatus status
) {
}
