package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.AccrualType;
import com.payhub.bankcore.domain.enums.DcDirection;
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
public class AccrualRecord {
    private LocalDate businessDate;
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String subjectCode;
    private AccrualType accrualType;
    private DcDirection dcDirection;
    private BigDecimal accrualAmount;
    private String status;
}
