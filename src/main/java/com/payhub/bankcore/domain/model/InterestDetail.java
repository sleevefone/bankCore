package com.payhub.bankcore.domain.model;

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
public class InterestDetail {
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String subjectCode;
    private BigDecimal interestRate;
    private BigDecimal interestBaseAmount;
    private BigDecimal accruedInterest;
    private BigDecimal settledInterest;
    private LocalDate lastAccrualDate;
}
