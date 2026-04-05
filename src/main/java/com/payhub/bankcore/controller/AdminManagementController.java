package com.payhub.bankcore.controller;

import com.payhub.bankcore.application.dto.admin.AccountView;
import com.payhub.bankcore.application.dto.admin.AccountPageResult;
import com.payhub.bankcore.application.dto.admin.CreateCustomerRequest;
import com.payhub.bankcore.application.dto.admin.CustomerPageResult;
import com.payhub.bankcore.application.dto.admin.CustomerView;
import com.payhub.bankcore.application.dto.admin.OpenAccountRequest;
import com.payhub.bankcore.application.service.AdminManagementService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @PostMapping("/customers")
    public CustomerView createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return adminManagementService.createCustomer(request);
    }

    @GetMapping("/customers/{customerNo}")
    public CustomerView getCustomer(@PathVariable String customerNo) {
        return adminManagementService.getCustomer(customerNo);
    }

    @GetMapping({"/customers", "/customers/"})
    public CustomerPageResult searchCustomers(
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String mobile,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize
    ) {
        return adminManagementService.searchCustomers(customerNo, customerName, mobile, pageNo, pageSize);
    }

    @GetMapping("/customers/{customerNo}/accounts")
    public List<AccountView> listAccountsByCustomer(@PathVariable String customerNo) {
        return adminManagementService.listAccountsByCustomer(customerNo);
    }

    @GetMapping({"/accounts", "/accounts/"})
    public AccountPageResult searchAccounts(
            @RequestParam(required = false) String customerNo,
            @RequestParam(required = false) String accountNo,
            @RequestParam(required = false) String accountType,
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "20") long pageSize
    ) {
        return adminManagementService.searchAccounts(customerNo, accountNo, accountType, pageNo, pageSize);
    }

    @GetMapping({"/accounts/{accountNo}", "/accounts/{accountNo}/"})
    public AccountView getAccount(@PathVariable String accountNo) {
        return adminManagementService.getAccount(accountNo);
    }

    @PostMapping("/accounts/deposit")
    public AccountView openDepositAccount(@Valid @RequestBody OpenAccountRequest request) {
        return adminManagementService.openDepositAccount(request);
    }

    @PostMapping("/accounts/loan")
    public AccountView openLoanAccount(@Valid @RequestBody OpenAccountRequest request) {
        return adminManagementService.openLoanAccount(request);
    }
}
