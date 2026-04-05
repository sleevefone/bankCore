package com.payhub.bankcore.application.dto.admin;

import java.util.List;
import lombok.Data;

@Data
public class AccountPageResult {
    private long pageNo;
    private long pageSize;
    private long total;
    private List<AccountView> records;
}
