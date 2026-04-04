package com.payhub.bankcore.application.service;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
import com.payhub.bankcore.common.JacksonUtils;
import com.payhub.bankcore.domain.enums.AccountStatus;
import com.payhub.bankcore.domain.enums.BalanceDirection;
import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.enums.DcDirection;
import com.payhub.bankcore.domain.enums.EntryDirection;
import com.payhub.bankcore.domain.model.Account;
import com.payhub.bankcore.domain.model.AuditLog;
import com.payhub.bankcore.domain.model.CoreTransaction;
import com.payhub.bankcore.domain.model.LedgerEntry;
import com.payhub.bankcore.infrastructure.persistence.repository.AccountRepository;
import com.payhub.bankcore.infrastructure.persistence.repository.AuditLogRepository;
import com.payhub.bankcore.infrastructure.persistence.repository.CoreTransactionRepository;
import com.payhub.bankcore.infrastructure.persistence.repository.LedgerEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class CoreTransactionApplicationService {

    private final CoreTransactionRepository coreTransactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogRepository auditLogRepository;

    public CoreTransactionApplicationService(
            CoreTransactionRepository coreTransactionRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.coreTransactionRepository = coreTransactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public CoreTransactionResponse create(CreateCoreTransactionRequest request) {
        CoreTransaction existing = coreTransactionRepository.findByRequestId(request.getRequestId()).orElse(null);
        if (existing != null) {
            return toResponse(existing, false, false, "IDEMPOTENT_HIT", "Request already accepted");
        }

        Account debitAccount = validateAccount(
                request.getDebitAccountNo(),
                request.getDebitAccountSeqNo(),
                request.getCustomerNo(),
                request.getDebitSubjectCode()
        );
        Account creditAccount = validateAccount(
                request.getCreditAccountNo(),
                request.getCreditAccountSeqNo(),
                request.getCustomerNo(),
                request.getCreditSubjectCode()
        );

        BigDecimal debitBalanceAfter = applyPosting(debitAccount.getAvailableBalance(), request.getAmount(), debitAccount.getNormalBalanceDirection(), DcDirection.DEBIT);
        BigDecimal creditBalanceAfter = applyPosting(creditAccount.getAvailableBalance(), request.getAmount(), creditAccount.getNormalBalanceDirection(), DcDirection.CREDIT);

        LocalDateTime now = LocalDateTime.now();
        CoreTransaction transaction = new CoreTransaction(
                "CTX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                request.getRequestId(),
                request.getBizOrderId(),
                request.getBizType(),
                request.getTxnType(),
                request.getCustomerNo(),
                request.getAmount(),
                request.getCurrency(),
                request.getDebitAccountNo(),
                request.getDebitAccountSeqNo(),
                request.getDebitSubjectCode(),
                request.getCreditAccountNo(),
                request.getCreditAccountSeqNo(),
                request.getCreditSubjectCode(),
                CoreTransactionStatus.SUCCESS,
                null,
                null,
                request.getOccurredAt(),
                now
        );

        coreTransactionRepository.save(transaction);
        ledgerEntryRepository.saveAll(List.of(
                new LedgerEntry(
                        transaction.getCoreTxnId(),
                        1,
                        debitAccount.getAccountNo(),
                        debitAccount.getAccountSeqNo(),
                        debitAccount.getCustomerNo(),
                        debitAccount.getSubjectCode(),
                        EntryDirection.PRINCIPAL,
                        DcDirection.DEBIT,
                        request.getAmount(),
                        request.getCurrency(),
                        debitAccount.getAvailableBalance(),
                        debitBalanceAfter
                ),
                new LedgerEntry(
                        transaction.getCoreTxnId(),
                        2,
                        creditAccount.getAccountNo(),
                        creditAccount.getAccountSeqNo(),
                        creditAccount.getCustomerNo(),
                        creditAccount.getSubjectCode(),
                        EntryDirection.PRINCIPAL,
                        DcDirection.CREDIT,
                        request.getAmount(),
                        request.getCurrency(),
                        creditAccount.getAvailableBalance(),
                        creditBalanceAfter
                )
        ));
        accountRepository.updateAvailableBalance(debitAccount.getAccountNo(), debitBalanceAfter);
        accountRepository.updateAvailableBalance(creditAccount.getAccountNo(), creditBalanceAfter);
        auditLogRepository.save(new AuditLog(
                "CORE_TRANSACTION",
                transaction.getCoreTxnId(),
                "CREATE",
                "system",
                request.getRequestId(),
                JacksonUtils.toJson(buildBeforeSnapshot(request)),
                JacksonUtils.toJson(buildAfterSnapshot(transaction, debitBalanceAfter, creditBalanceAfter)),
                now
        ));
        return toResponse(transaction, false, true, "POSTED", "Transaction posted successfully");
    }

    public CoreTransactionResponse getByBizOrderId(String bizOrderId) {
        CoreTransaction transaction = coreTransactionRepository.findByBizOrderId(bizOrderId).orElse(null);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, transaction.getStatus() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    public CoreTransactionResponse getByRequestId(String requestId) {
        CoreTransaction transaction = coreTransactionRepository.findByRequestId(requestId).orElse(null);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, transaction.getStatus() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    private CoreTransactionResponse toResponse(
            CoreTransaction transaction,
            boolean retryable,
            boolean newlyAccepted,
            String rawCode,
            String rawMessage
    ) {
        return new CoreTransactionResponse(
                transaction.getCoreTxnId(),
                transaction.getRequestId(),
                transaction.getBizOrderId(),
                transaction.getBizType(),
                transaction.getTxnType(),
                transaction.getCustomerNo(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDebitAccountNo(),
                transaction.getDebitAccountSeqNo(),
                transaction.getDebitSubjectCode(),
                transaction.getCreditAccountNo(),
                transaction.getCreditAccountSeqNo(),
                transaction.getCreditSubjectCode(),
                transaction.getStatus(),
                transaction.getStatus() == CoreTransactionStatus.SUCCESS,
                retryable,
                rawCode,
                rawMessage,
                transaction.getOccurredAt(),
                transaction.getCreatedAt()
        );
    }

    private Account validateAccount(
            String accountNo,
            Long accountSeqNo,
            String customerNo,
            String subjectCode
    ) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Account not found: " + accountNo));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Account is not active: " + accountNo);
        }
        if (!Objects.equals(account.getAccountSeqNo(), accountSeqNo)) {
            throw new ResponseStatusException(BAD_REQUEST, "Account sequence mismatch: " + accountNo);
        }
        if (!Objects.equals(account.getCustomerNo(), customerNo)) {
            throw new ResponseStatusException(BAD_REQUEST, "Customer number mismatch: " + accountNo);
        }
        if (!Objects.equals(account.getSubjectCode(), subjectCode)) {
            throw new ResponseStatusException(BAD_REQUEST, "Subject code mismatch: " + accountNo);
        }
        return account;
    }

    private BigDecimal applyPosting(
            BigDecimal currentBalance,
            BigDecimal amount,
            BalanceDirection normalBalanceDirection,
            DcDirection dcDirection
    ) {
        if (normalBalanceDirection.name().equals(dcDirection.name())) {
            return currentBalance.add(amount);
        }
        return currentBalance.subtract(amount);
    }

    private Map<String, Object> buildBeforeSnapshot(CreateCoreTransactionRequest request) {
        return Map.ofEntries(
                Map.entry("requestId", request.getRequestId()),
                Map.entry("bizOrderId", request.getBizOrderId()),
                Map.entry("customerNo", request.getCustomerNo()),
                Map.entry("txnType", request.getTxnType().name()),
                Map.entry("occurredAt", request.getOccurredAt()),
                Map.entry("debitAccountNo", request.getDebitAccountNo()),
                Map.entry("debitAccountSeqNo", request.getDebitAccountSeqNo()),
                Map.entry("creditAccountNo", request.getCreditAccountNo()),
                Map.entry("creditAccountSeqNo", request.getCreditAccountSeqNo()),
                Map.entry("amount", request.getAmount()),
                Map.entry("currency", request.getCurrency())
        );
    }

    private Map<String, Object> buildAfterSnapshot(
            CoreTransaction transaction,
            BigDecimal debitBalanceAfter,
            BigDecimal creditBalanceAfter
    ) {
        return Map.ofEntries(
                Map.entry("coreTxnId", transaction.getCoreTxnId()),
                Map.entry("status", transaction.getStatus().name()),
                Map.entry("createdAt", transaction.getCreatedAt()),
                Map.entry("occurredAt", transaction.getOccurredAt()),
                Map.entry("debitAccountNo", transaction.getDebitAccountNo()),
                Map.entry("debitAccountSeqNo", transaction.getDebitAccountSeqNo()),
                Map.entry("creditAccountNo", transaction.getCreditAccountNo()),
                Map.entry("creditAccountSeqNo", transaction.getCreditAccountSeqNo()),
                Map.entry("debitSubjectCode", transaction.getDebitSubjectCode()),
                Map.entry("creditSubjectCode", transaction.getCreditSubjectCode()),
                Map.entry("amount", transaction.getAmount()),
                Map.entry("currency", transaction.getCurrency()),
                Map.entry("debitBalanceAfter", debitBalanceAfter),
                Map.entry("creditBalanceAfter", creditBalanceAfter)
        );
    }
}
