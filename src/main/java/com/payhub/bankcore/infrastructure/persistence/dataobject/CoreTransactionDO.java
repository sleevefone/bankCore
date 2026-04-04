package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("core_transaction")
public class CoreTransactionDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String coreTxnId;
    private String requestId;
    private String bizOrderId;
    private String bizType;
    private String txnType;
    private String customerNo;
    private BigDecimal amount;
    private String currency;
    private String debitAccountNo;
    private Long debitAccountSeqNo;
    private String debitSubjectCode;
    private String creditAccountNo;
    private Long creditAccountSeqNo;
    private String creditSubjectCode;
    private String status;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
