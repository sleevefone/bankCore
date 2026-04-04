package com.payhub.bankcore.application.service;

import com.payhub.bankcore.application.dto.CoreTransactionResponse;
import com.payhub.bankcore.application.dto.CreateCoreTransactionRequest;
import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.model.CoreTransaction;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class CoreTransactionApplicationService {

    private final Map<String, CoreTransaction> transactionsByRequestId = new ConcurrentHashMap<>();
    private final Map<String, CoreTransaction> transactionsByBizOrderId = new ConcurrentHashMap<>();

    public CoreTransactionResponse create(CreateCoreTransactionRequest request) {
        CoreTransaction existing = transactionsByRequestId.get(request.requestId());
        if (existing != null) {
            return toResponse(existing, true, false, "IDEMPOTENT_HIT", "Request already accepted");
        }

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
                CoreTransactionStatus.INIT,
                null,
                null,
                request.occurredAt(),
                now
        );

        transactionsByRequestId.put(transaction.requestId(), transaction);
        transactionsByBizOrderId.put(transaction.bizOrderId(), transaction);
        return toResponse(transaction, false, true, "ACCEPTED", "Bootstrap mode: transaction captured");
    }

    public CoreTransactionResponse getByBizOrderId(String bizOrderId) {
        CoreTransaction transaction = transactionsByBizOrderId.get(bizOrderId);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, true, false, "FOUND", "Transaction found");
    }

    public CoreTransactionResponse getByRequestId(String requestId) {
        CoreTransaction transaction = transactionsByRequestId.get(requestId);
        if (transaction == null) {
            return null;
        }
        return toResponse(transaction, true, false, "FOUND", "Transaction found");
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
                false,
                retryable,
                rawCode,
                newlyAccepted ? rawMessage : "Transaction already captured",
                transaction.occurredAt(),
                transaction.createdAt()
        );
    }
}
