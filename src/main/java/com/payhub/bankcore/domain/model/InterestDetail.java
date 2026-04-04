package com.payhub.bankcore.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InterestDetail(
        String accountNo,
        Long accountSeqNo,
        String customerNo,
        String subjectCode,
        BigDecimal interestRate,
        BigDecimal interestBaseAmount,
        BigDecimal accruedInterest,
        BigDecimal settledInterest,
        LocalDate lastAccrualDate
) {
}
