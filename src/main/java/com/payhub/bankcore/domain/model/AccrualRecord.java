package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.AccrualType;
import com.payhub.bankcore.domain.enums.DcDirection;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AccrualRecord(
        LocalDate businessDate,
        String accountNo,
        Long accountSeqNo,
        String customerNo,
        String subjectCode,
        AccrualType accrualType,
        DcDirection dcDirection,
        BigDecimal accrualAmount,
        String status
) {
}
