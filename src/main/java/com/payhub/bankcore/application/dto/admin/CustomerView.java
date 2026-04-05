package com.payhub.bankcore.application.dto.admin;

import lombok.Data;

@Data
public class CustomerView {
    private String customerNo;
    private String customerName;
    private String mobile;
    private String idNo;
    private String status;
}
