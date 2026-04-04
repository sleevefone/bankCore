package com.payhub.bankcore.domain.model;

import com.payhub.bankcore.domain.enums.BalanceDirection;
import com.payhub.bankcore.domain.enums.SubjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    private String subjectCode;
    private String subjectName;
    private Integer subjectLevel;
    private String parentSubjectCode;
    private BalanceDirection normalBalanceDirection;
    private boolean interestBearing;
    private SubjectStatus status;
}
