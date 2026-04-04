package com.payhub.bankcore.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payhub.bankcore.domain.enums.CoreTransactionStatus;
import com.payhub.bankcore.domain.enums.TxnType;
import com.payhub.bankcore.domain.model.CoreTransaction;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreTransactionDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreTransactionMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class CoreTransactionRepository {

    private final CoreTransactionMapper coreTransactionMapper;

    public CoreTransactionRepository(CoreTransactionMapper coreTransactionMapper) {
        this.coreTransactionMapper = coreTransactionMapper;
    }

    public Optional<CoreTransaction> findByRequestId(String requestId) {
        return Optional.ofNullable(coreTransactionMapper.selectOne(
                new LambdaQueryWrapper<CoreTransactionDO>().eq(CoreTransactionDO::getRequestId, requestId), false
        )).map(this::toDomain);
    }

    public Optional<CoreTransaction> findByBizOrderId(String bizOrderId) {
        return Optional.ofNullable(coreTransactionMapper.selectOne(
                new LambdaQueryWrapper<CoreTransactionDO>().eq(CoreTransactionDO::getBizOrderId, bizOrderId), false
        )).map(this::toDomain);
    }

    public void save(CoreTransaction transaction) {
        coreTransactionMapper.insert(toDataObject(transaction));
    }

    private CoreTransactionDO toDataObject(CoreTransaction transaction) {
        CoreTransactionDO dataObject = new CoreTransactionDO();
        dataObject.setCoreTxnId(transaction.coreTxnId());
        dataObject.setRequestId(transaction.requestId());
        dataObject.setBizOrderId(transaction.bizOrderId());
        dataObject.setBizType(transaction.bizType());
        dataObject.setTxnType(transaction.txnType().name());
        dataObject.setCustomerNo(transaction.customerNo());
        dataObject.setAmount(transaction.amount());
        dataObject.setCurrency(transaction.currency());
        dataObject.setDebitAccountNo(transaction.debitAccountNo());
        dataObject.setDebitAccountSeqNo(transaction.debitAccountSeqNo());
        dataObject.setDebitSubjectCode(transaction.debitSubjectCode());
        dataObject.setCreditAccountNo(transaction.creditAccountNo());
        dataObject.setCreditAccountSeqNo(transaction.creditAccountSeqNo());
        dataObject.setCreditSubjectCode(transaction.creditSubjectCode());
        dataObject.setStatus(transaction.status().name());
        dataObject.setFailureCode(transaction.failureCode());
        dataObject.setFailureMessage(transaction.failureMessage());
        dataObject.setOccurredAt(transaction.occurredAt());
        dataObject.setCreatedAt(transaction.createdAt());
        dataObject.setUpdatedAt(transaction.createdAt());
        return dataObject;
    }

    private CoreTransaction toDomain(CoreTransactionDO dataObject) {
        return new CoreTransaction(
                dataObject.getCoreTxnId(),
                dataObject.getRequestId(),
                dataObject.getBizOrderId(),
                dataObject.getBizType(),
                TxnType.valueOf(dataObject.getTxnType()),
                dataObject.getCustomerNo(),
                dataObject.getAmount(),
                dataObject.getCurrency(),
                dataObject.getDebitAccountNo(),
                dataObject.getDebitAccountSeqNo(),
                dataObject.getDebitSubjectCode(),
                dataObject.getCreditAccountNo(),
                dataObject.getCreditAccountSeqNo(),
                dataObject.getCreditSubjectCode(),
                CoreTransactionStatus.valueOf(dataObject.getStatus()),
                dataObject.getFailureCode(),
                dataObject.getFailureMessage(),
                dataObject.getOccurredAt(),
                dataObject.getCreatedAt()
        );
    }
}
