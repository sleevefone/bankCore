package com.payhub.bankcore.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payhub.bankcore.common.JacksonUtils;
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
        )).map(dataObject -> JacksonUtils.convertValue(dataObject, CoreTransaction.class));
    }

    public Optional<CoreTransaction> findByBizOrderId(String bizOrderId) {
        return Optional.ofNullable(coreTransactionMapper.selectOne(
                new LambdaQueryWrapper<CoreTransactionDO>().eq(CoreTransactionDO::getBizOrderId, bizOrderId), false
        )).map(dataObject -> JacksonUtils.convertValue(dataObject, CoreTransaction.class));
    }

    public void save(CoreTransaction transaction) {
        coreTransactionMapper.insert(toDataObject(transaction));
    }

    private CoreTransactionDO toDataObject(CoreTransaction transaction) {
        CoreTransactionDO dataObject = JacksonUtils.convertValue(transaction, CoreTransactionDO.class);
        dataObject.setUpdatedAt(transaction.getCreatedAt());
        return dataObject;
    }
}
