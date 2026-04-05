package com.payhub.bankcore.application.service;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
import com.payhub.bankcore.common.JacksonMapper;
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
import com.payhub.bankcore.infrastructure.persistence.repository.CoreTransactionHistoryRepository;
import com.payhub.bankcore.infrastructure.persistence.repository.LedgerEntryRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@Service
public class CoreTransactionApplicationService {

    private final CoreTransactionRepository coreTransactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogRepository auditLogRepository;
    private final CoreTransactionHistoryRepository coreTransactionHistoryRepository;

    public CoreTransactionApplicationService(
            CoreTransactionRepository coreTransactionRepository,
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AuditLogRepository auditLogRepository,
            CoreTransactionHistoryRepository coreTransactionHistoryRepository
    ) {
        this.coreTransactionRepository = coreTransactionRepository;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.auditLogRepository = auditLogRepository;
        this.coreTransactionHistoryRepository = coreTransactionHistoryRepository;
    }

    @Transactional
    public CoreTransactionResponse create(CreateCoreTransactionRequest request) {
        log.info("Received core transaction request: requestId={}, bizOrderId={}, txnType={}, customerNo={}, amount={}, currency={}, debitAccountNo={}, creditAccountNo={}",
                request.getRequestId(), request.getBizOrderId(), request.getTxnType(), request.getCustomerNo(),
                request.getAmount(), request.getCurrency(), request.getDebitAccountNo(), request.getCreditAccountNo());
        CoreTransaction existing = coreTransactionRepository.findByRequestId(request.getRequestId()).orElse(null);
        if (existing != null) {
            log.info("Detected idempotent hit for core transaction request: requestId={}, existingCoreTxnId={}, status={}",
                    request.getRequestId(), existing.getCoreTxnId(), existing.getStatus());
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
        CoreTransaction transaction = buildTransaction(request, now);

        try {
            coreTransactionRepository.save(transaction);
            ledgerEntryRepository.saveAll(List.of(
                    buildLedgerEntry(transaction, debitAccount, request, 1, DcDirection.DEBIT, debitAccount.getAvailableBalance(), debitBalanceAfter),
                    buildLedgerEntry(transaction, creditAccount, request, 2, DcDirection.CREDIT, creditAccount.getAvailableBalance(), creditBalanceAfter)
            ));
            accountRepository.updateAvailableBalance(debitAccount.getAccountNo(), debitBalanceAfter);
            accountRepository.updateAvailableBalance(creditAccount.getAccountNo(), creditBalanceAfter);
            auditLogRepository.save(buildAuditLog(request, transaction, debitBalanceAfter, creditBalanceAfter, now));
            coreTransactionHistoryRepository.save(
                    transaction.getCoreTxnId(),
                    null,
                    transaction.getStatus().name(),
                    "POSTED",
                    "Core transaction posted successfully",
                    now
            );
        } catch (RuntimeException ex) {
            log.error("Failed to post core transaction: requestId={}, bizOrderId={}, coreTxnId={}, debitAccountNo={}, creditAccountNo={}",
                    request.getRequestId(), request.getBizOrderId(), transaction.getCoreTxnId(),
                    request.getDebitAccountNo(), request.getCreditAccountNo(), ex);
            throw ex;
        }
        log.info("Posted core transaction successfully: requestId={}, bizOrderId={}, coreTxnId={}, debitBalanceAfter={}, creditBalanceAfter={}",
                request.getRequestId(), request.getBizOrderId(), transaction.getCoreTxnId(), debitBalanceAfter, creditBalanceAfter);
        return toResponse(transaction, false, true, "POSTED", "Transaction posted successfully");
    }

    public CoreTransactionResponse getByBizOrderId(String bizOrderId) {
        CoreTransaction transaction = coreTransactionRepository.findByBizOrderId(bizOrderId).orElse(null);
        if (transaction == null) {
            log.warn("Core transaction not found by bizOrderId: {}", bizOrderId);
            return null;
        }
        log.info("Found core transaction by bizOrderId: bizOrderId={}, coreTxnId={}, status={}",
                bizOrderId, transaction.getCoreTxnId(), transaction.getStatus());
        return toResponse(transaction, transaction.getStatus() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    public CoreTransactionResponse getByRequestId(String requestId) {
        CoreTransaction transaction = coreTransactionRepository.findByRequestId(requestId).orElse(null);
        if (transaction == null) {
            log.warn("Core transaction not found by requestId: {}", requestId);
            return null;
        }
        log.info("Found core transaction by requestId: requestId={}, coreTxnId={}, status={}",
                requestId, transaction.getCoreTxnId(), transaction.getStatus());
        return toResponse(transaction, transaction.getStatus() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    private CoreTransactionResponse toResponse(CoreTransaction transaction, boolean retryable, boolean newlyAccepted, String rawCode, String rawMessage) {
        CoreTransactionResponse resp = JacksonMapper.convertValue(transaction, CoreTransactionResponse.class);
        resp.setSuccess(transaction.getStatus() == CoreTransactionStatus.SUCCESS);
        resp.setRetryable(retryable);
        resp.setRawCode(rawCode);
        resp.setRawMessage(rawMessage);
        return resp;
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

    private CoreTransaction buildTransaction(CreateCoreTransactionRequest request, LocalDateTime now) {
        CoreTransaction transaction = JacksonMapper.convertValue(request, CoreTransaction.class);
        transaction.setCoreTxnId("CTX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        transaction.setStatus(CoreTransactionStatus.SUCCESS);
        transaction.setCreatedAt(now);
        return transaction;
    }

    private LedgerEntry buildLedgerEntry(
            CoreTransaction transaction,
            Account account,
            CreateCoreTransactionRequest request,
            Integer entryNo,
            DcDirection dcDirection,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter
    ) {
        Map<String, Object> source = new LinkedHashMap<>(JacksonMapper.toMap(transaction));
        source.putAll(JacksonMapper.toMap(account));
        source.putAll(JacksonMapper.toMap(request));
        source.put("entryNo", entryNo);
        source.put("entryDirection", EntryDirection.PRINCIPAL);
        source.put("dcDirection", dcDirection);
        source.put("balanceBefore", balanceBefore);
        source.put("balanceAfter", balanceAfter);
        return JacksonMapper.convertValue(source, LedgerEntry.class);
    }

    private AuditLog buildAuditLog(
            CreateCoreTransactionRequest request,
            CoreTransaction transaction,
            BigDecimal debitBalanceAfter,
            BigDecimal creditBalanceAfter,
            LocalDateTime now
    ) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("entityType", "CORE_TRANSACTION");
        source.put("entityId", transaction.getCoreTxnId());
        source.put("operationType", "CREATE");
        source.put("operatorId", "system");
        source.put("traceId", request.getRequestId());
        source.put("beforeSnapshot", JacksonMapper.toJson(buildBeforeSnapshot(request)));
        source.put("afterSnapshot", JacksonMapper.toJson(buildAfterSnapshot(transaction, debitBalanceAfter, creditBalanceAfter)));
        source.put("createdAt", now);
        return JacksonMapper.convertValue(source, AuditLog.class);
    }

    private Map<String, Object> buildBeforeSnapshot(CreateCoreTransactionRequest request) {
        Map<String, Object> snapshot = new LinkedHashMap<>(JacksonMapper.toMap(request));
        snapshot.put("txnType", request.getTxnType().name());
        return snapshot;
    }

    private Map<String, Object> buildAfterSnapshot(
            CoreTransaction transaction,
            BigDecimal debitBalanceAfter,
            BigDecimal creditBalanceAfter
    ) {
        Map<String, Object> snapshot = new LinkedHashMap<>(JacksonMapper.toMap(transaction));
        snapshot.put("status", transaction.getStatus().name());
        snapshot.put("debitBalanceAfter", debitBalanceAfter);
        snapshot.put("creditBalanceAfter", creditBalanceAfter);
        return snapshot;
    }
}
