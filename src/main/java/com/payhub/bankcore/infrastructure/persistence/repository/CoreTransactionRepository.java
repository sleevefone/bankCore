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
        dataObject.setCoreTxnId(transaction.getCoreTxnId());
        dataObject.setRequestId(transaction.getRequestId());
        dataObject.setBizOrderId(transaction.getBizOrderId());
        dataObject.setBizType(transaction.getBizType());
        dataObject.setTxnType(transaction.getTxnType().name());
        dataObject.setCustomerNo(transaction.getCustomerNo());
        dataObject.setAmount(transaction.getAmount());
        dataObject.setCurrency(transaction.getCurrency());
        dataObject.setDebitAccountNo(transaction.getDebitAccountNo());
        dataObject.setDebitAccountSeqNo(transaction.getDebitAccountSeqNo());
        dataObject.setDebitSubjectCode(transaction.getDebitSubjectCode());
        dataObject.setCreditAccountNo(transaction.getCreditAccountNo());
        dataObject.setCreditAccountSeqNo(transaction.getCreditAccountSeqNo());
        dataObject.setCreditSubjectCode(transaction.getCreditSubjectCode());
        dataObject.setStatus(transaction.getStatus().name());
        dataObject.setFailureCode(transaction.getFailureCode());
        dataObject.setFailureMessage(transaction.getFailureMessage());
        dataObject.setOccurredAt(transaction.getOccurredAt());
        dataObject.setCreatedAt(transaction.getCreatedAt());
        dataObject.setUpdatedAt(transaction.getCreatedAt());
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
