package com.payhub.bankcore.application.service;

import com.payhub.bankcore.application.dto.admin.AccountView;
import com.payhub.bankcore.application.dto.admin.AccountPageResult;
import com.payhub.bankcore.application.dto.admin.CreateCustomerRequest;
import com.payhub.bankcore.application.dto.admin.CustomerPageResult;
import com.payhub.bankcore.application.dto.admin.CustomerView;
import com.payhub.bankcore.application.dto.admin.OpenAccountRequest;
import com.payhub.bankcore.common.JacksonMapper;
import com.payhub.bankcore.domain.enums.AccountStatus;
import com.payhub.bankcore.domain.enums.AccountType;
import com.payhub.bankcore.domain.model.Account;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreCustomerDO;
import com.payhub.bankcore.infrastructure.persistence.repository.AccountRepository;
import com.payhub.bankcore.infrastructure.persistence.repository.CoreCustomerRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AdminManagementService {
    private static final String BANK_CODE = "8801";
    private static final DateTimeFormatter ACCOUNT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CoreCustomerRepository coreCustomerRepository;
    private final AccountRepository accountRepository;

    public AdminManagementService(
            CoreCustomerRepository coreCustomerRepository,
            AccountRepository accountRepository
    ) {
        this.coreCustomerRepository = coreCustomerRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public CustomerView createCustomer(CreateCustomerRequest request) {
        if (coreCustomerRepository.findByCustomerNo(request.getCustomerNo()).isPresent()) {
            throw new ResponseStatusException(BAD_REQUEST, "Customer already exists: " + request.getCustomerNo());
        }
        LocalDateTime now = LocalDateTime.now();
        CoreCustomerDO customer = new CoreCustomerDO();
        customer.setCustomerNo(request.getCustomerNo());
        customer.setCustomerName(request.getCustomerName());
        customer.setMobile(request.getMobile());
        customer.setIdNo(request.getIdNo());
        customer.setStatus("ACTIVE");
        customer.setCreatedAt(now);
        customer.setUpdatedAt(now);
        coreCustomerRepository.save(customer);
        return JacksonMapper.convertValue(customer, CustomerView.class);
    }

    public CustomerView getCustomer(String customerNo) {
        CoreCustomerDO customer = coreCustomerRepository.findByCustomerNo(customerNo)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found: " + customerNo));
        return JacksonMapper.convertValue(customer, CustomerView.class);
    }

    public CustomerPageResult searchCustomers(String customerNo, String customerName, String mobile, long pageNo, long pageSize) {
        long safePageNo = Math.max(pageNo, 1);
        long safePageSize = Math.clamp(pageSize, 1, 200);
        List<CustomerView> all = coreCustomerRepository.search(customerNo, customerName, mobile).stream()
                .map(customer -> JacksonMapper.convertValue(customer, CustomerView.class))
                .toList();
        int from = (int) Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = (int) Math.min(from + safePageSize, all.size());

        CustomerPageResult result = new CustomerPageResult();
        result.setPageNo(safePageNo);
        result.setPageSize(safePageSize);
        result.setTotal(all.size());
        result.setRecords(all.subList(from, to));
        return result;
    }

    @Transactional
    public AccountView openDepositAccount(OpenAccountRequest request) {
        return openAccount(request, AccountType.DEPOSIT, "D");
    }

    @Transactional
    public AccountView openLoanAccount(OpenAccountRequest request) {
        return openAccount(request, AccountType.LOAN, "L");
    }

    public List<AccountView> listAccountsByCustomer(String customerNo) {
        if (coreCustomerRepository.findByCustomerNo(customerNo).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Customer not found: " + customerNo);
        }
        return accountRepository.findByCustomerNo(customerNo).stream()
                .map(account -> JacksonMapper.convertValue(account, AccountView.class))
                .toList();
    }

    public AccountView getAccount(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Account not found: " + accountNo));
        return JacksonMapper.convertValue(account, AccountView.class);
    }

    public AccountPageResult searchAccounts(String customerNo, String accountNo, String accountType, long pageNo, long pageSize) {
        long safePageNo = Math.max(pageNo, 1);
        long safePageSize = Math.clamp(pageSize, 1, 200);
        List<AccountView> all = accountRepository.search(customerNo, accountNo, accountType).stream()
                .map(account -> JacksonMapper.convertValue(account, AccountView.class))
                .toList();
        int from = (int) Math.min((safePageNo - 1) * safePageSize, all.size());
        int to = (int) Math.min(from + safePageSize, all.size());

        AccountPageResult result = new AccountPageResult();
        result.setPageNo(safePageNo);
        result.setPageSize(safePageSize);
        result.setTotal(all.size());
        result.setRecords(all.subList(from, to));
        return result;
    }

    private AccountView openAccount(OpenAccountRequest request, AccountType accountType, String prefix) {
        if (coreCustomerRepository.findByCustomerNo(request.getCustomerNo()).isEmpty()) {
            throw new ResponseStatusException(NOT_FOUND, "Customer not found: " + request.getCustomerNo());
        }
        Account account = Account.builder()
                .accountNo(generateAccountNo(prefix))
                .accountSeqNo(generateAccountSeqNo())
                .customerNo(request.getCustomerNo())
                .accountType(accountType)
                .subjectCode(request.getSubjectCode())
                .normalBalanceDirection(request.getNormalBalanceDirection())
                .ownerId(request.getCustomerNo())
                .currency(request.getCurrency())
                .availableBalance(BigDecimal.ZERO)
                .frozenBalance(BigDecimal.ZERO)
                .interestRate(request.getInterestRate())
                .status(AccountStatus.ACTIVE)
                .build();
        accountRepository.save(account);
        return JacksonMapper.convertValue(account, AccountView.class);
    }

    private String generateAccountNo(String prefix) {
        String typeCode = switch (prefix) {
            case "D" -> "1";
            case "L" -> "2";
            default -> "9";
        };
        String datePart = LocalDateTime.now().format(ACCOUNT_DATE_FORMATTER);
        String serialPart = String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000));
        String base = BANK_CODE + typeCode + datePart + serialPart;
        int checkDigit = luhnCheckDigit(base);
        return base + checkDigit;
    }

    private int luhnCheckDigit(String base) {
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = base.length() - 1; i >= 0; i--) {
            int digit = base.charAt(i) - '0';
            if (doubleDigit) {
                digit = digit * 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }

    private long generateAccountSeqNo() {
        long millis = System.currentTimeMillis();
        int suffix = ThreadLocalRandom.current().nextInt(100, 999);
        return millis * 1000 + suffix;
    }
}
