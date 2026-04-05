package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("core_transaction_history")
public class CoreTransactionHistoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String coreTxnId;
    private String beforeStatus;
    private String afterStatus;
    private String eventType;
    private String message;
    private LocalDateTime createdAt;
}
