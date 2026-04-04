package com.payhub.bankcore.application.service;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
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
        CoreTransaction existing = coreTransactionRepository.findByRequestId(request.requestId()).orElse(null);
        if (existing != null) {
            return toResponse(existing, false, false, "IDEMPOTENT_HIT", "Request already accepted");
        }

        Account debitAccount = validateAccount(
                request.debitAccountNo(),
                request.debitAccountSeqNo(),
                request.customerNo(),
                request.debitSubjectCode()
        );
        Account creditAccount = validateAccount(
                request.creditAccountNo(),
                request.creditAccountSeqNo(),
                request.customerNo(),
                request.creditSubjectCode()
        );

        BigDecimal debitBalanceAfter = applyPosting(debitAccount.availableBalance(), request.amount(), debitAccount.normalBalanceDirection(), DcDirection.DEBIT);
        BigDecimal creditBalanceAfter = applyPosting(creditAccount.availableBalance(), request.amount(), creditAccount.normalBalanceDirection(), DcDirection.CREDIT);

        LocalDateTime now = LocalDateTime.now();
        CoreTransaction transaction = new CoreTransaction(
                "CTX-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20),
                request.requestId(),
                request.bizOrderId(),
                request.bizType(),
                request.txnType(),
                request.customerNo(),
                request.amount(),
                request.currency(),
                request.debitAccountNo(),
                request.debitAccountSeqNo(),
                request.debitSubjectCode(),
                request.creditAccountNo(),
                request.creditAccountSeqNo(),
                request.creditSubjectCode(),
                CoreTransactionStatus.SUCCESS,
                null,
                null,
                request.occurredAt(),
                now
        );

        coreTransactionRepository.save(transaction);
        ledgerEntryRepository.saveAll(List.of(
                new LedgerEntry(
                        transaction.coreTxnId(),
                        1,
                        debitAccount.accountNo(),
                        debitAccount.accountSeqNo(),
                        debitAccount.customerNo(),
                        debitAccount.subjectCode(),
                        EntryDirection.PRINCIPAL,
                        DcDirection.DEBIT,
                        request.amount(),
                        request.currency(),
                        debitAccount.availableBalance(),
                        debitBalanceAfter
                ),
                new LedgerEntry(
                        transaction.coreTxnId(),
                        2,
                        creditAccount.accountNo(),
                        creditAccount.accountSeqNo(),
                        creditAccount.customerNo(),
                        creditAccount.subjectCode(),
                        EntryDirection.PRINCIPAL,
                        DcDirection.CREDIT,
                        request.amount(),
                        request.currency(),
                        creditAccount.availableBalance(),
                        creditBalanceAfter
                )
        ));
        accountRepository.updateAvailableBalance(debitAccount.accountNo(), debitBalanceAfter);
        accountRepository.updateAvailableBalance(creditAccount.accountNo(), creditBalanceAfter);
        auditLogRepository.save(new AuditLog(
                "CORE_TRANSACTION",
                transaction.coreTxnId(),
                "CREATE",
                "system",
                request.requestId(),
                null,
                "requestId=" + request.requestId() + ",bizOrderId=" + request.bizOrderId() + ",amount=" + request.amount(),
                now
        ));
        return toResponse(transaction, false, true, "POSTED", "Transaction posted successfully");
    }

    public CoreTransactionResponse getByBizOrderId(String bizOrderId) {
        CoreTransaction transaction = coreTransactionRepository.findByBizOrderId(bizOrderId).orElse(null);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, transaction.status() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    public CoreTransactionResponse getByRequestId(String requestId) {
        CoreTransaction transaction = coreTransactionRepository.findByRequestId(requestId).orElse(null);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, transaction.status() != CoreTransactionStatus.SUCCESS, false, "FOUND", "Transaction found");
    }

    private CoreTransactionResponse toResponse(
            CoreTransaction transaction,
            boolean retryable,
            boolean newlyAccepted,
            String rawCode,
            String rawMessage
    ) {
        return new CoreTransactionResponse(
                transaction.coreTxnId(),
                transaction.requestId(),
                transaction.bizOrderId(),
                transaction.bizType(),
                transaction.txnType(),
                transaction.customerNo(),
                transaction.amount(),
                transaction.currency(),
                transaction.debitAccountNo(),
                transaction.debitAccountSeqNo(),
                transaction.debitSubjectCode(),
                transaction.creditAccountNo(),
                transaction.creditAccountSeqNo(),
                transaction.creditSubjectCode(),
                transaction.status(),
                transaction.status() == CoreTransactionStatus.SUCCESS,
                retryable,
                rawCode,
                rawMessage,
                transaction.occurredAt(),
                transaction.createdAt()
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
        if (account.status() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "Account is not active: " + accountNo);
        }
        if (!Objects.equals(account.accountSeqNo(), accountSeqNo)) {
            throw new ResponseStatusException(BAD_REQUEST, "Account sequence mismatch: " + accountNo);
        }
        if (!Objects.equals(account.customerNo(), customerNo)) {
            throw new ResponseStatusException(BAD_REQUEST, "Customer number mismatch: " + accountNo);
        }
        if (!Objects.equals(account.subjectCode(), subjectCode)) {
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
}
