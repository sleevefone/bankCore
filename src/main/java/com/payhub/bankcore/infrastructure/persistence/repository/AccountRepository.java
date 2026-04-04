package com.payhub.bankcore.infrastructure.persistence.repository;

import com.payhub.bankcore.common.JacksonMapper;
import com.payhub.bankcore.domain.model.Account;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreAccountDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreAccountMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private final CoreAccountMapper coreAccountMapper;

    public AccountRepository(CoreAccountMapper coreAccountMapper) {
        this.coreAccountMapper = coreAccountMapper;
    }

    public Optional<Account> findByAccountNo(String accountNo) {
        return Optional.ofNullable(coreAccountMapper.selectById(accountNo))
                .map(dataObject -> JacksonMapper.convertValue(dataObject, Account.class));
    }

    public void updateAvailableBalance(String accountNo, java.math.BigDecimal availableBalance) {
        CoreAccountDO existing = coreAccountMapper.selectById(accountNo);
        if (existing == null) {
            return;
        }
        existing.setAvailableBalance(availableBalance);
        coreAccountMapper.updateById(existing);
    }
}
