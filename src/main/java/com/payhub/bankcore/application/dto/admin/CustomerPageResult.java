package com.payhub.bankcore.application.dto.admin;

import java.util.List;
import lombok.Data;

@Data
public class CustomerPageResult {
    private long pageNo;
    private long pageSize;
    private long total;
    private List<CustomerView> records;
}
