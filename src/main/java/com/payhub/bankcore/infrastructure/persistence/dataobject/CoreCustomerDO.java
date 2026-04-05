package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("core_customer")
public class CoreCustomerDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerNo;
    private String customerName;
    private String mobile;
    private String idNo;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
