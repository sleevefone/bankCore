package com.payhub.bankcore.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCustomerRequest {
    @NotBlank
    private String customerNo;
    @NotBlank
    private String customerName;
    private String mobile;
    private String idNo;
}
