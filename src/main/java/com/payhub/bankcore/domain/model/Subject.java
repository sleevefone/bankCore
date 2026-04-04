package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.BalanceDirection;
import com.payhub.bankcore.domain.enums.SubjectStatus;

public record Subject(
        String subjectCode,
        String subjectName,
        Integer subjectLevel,
        String parentSubjectCode,
        BalanceDirection normalBalanceDirection,
        boolean interestBearing,
        SubjectStatus status
) {
}
