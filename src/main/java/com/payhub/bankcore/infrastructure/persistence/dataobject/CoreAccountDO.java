package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@TableName("core_account")
public class CoreAccountDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String accountType;
    private String subjectCode;
    private String normalBalanceDirection;
    private String ownerId;
    private String currency;
    private BigDecimal availableBalance;
    private BigDecimal frozenBalance;
    private BigDecimal interestRate;
    private LocalDate lastAccrualDate;
    private String status;
}
