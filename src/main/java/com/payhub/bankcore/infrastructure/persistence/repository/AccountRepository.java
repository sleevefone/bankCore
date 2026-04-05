package com.payhub.bankcore.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payhub.bankcore.common.JacksonMapper;
import com.payhub.bankcore.domain.model.Account;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreAccountDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreAccountMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Repository;

@Repository
public class AccountRepository {

    private final CoreAccountMapper coreAccountMapper;

    public AccountRepository(CoreAccountMapper coreAccountMapper) {
        this.coreAccountMapper = coreAccountMapper;
    }

    public Optional<Account> findByAccountNo(String accountNo) {
        return Optional.ofNullable(selectByAccountNo(accountNo))
                .map(dataObject -> JacksonMapper.convertValue(dataObject, Account.class));
    }

    public List<Account> findByCustomerNo(String customerNo) {
        return coreAccountMapper.selectList(
                new LambdaQueryWrapper<CoreAccountDO>().eq(CoreAccountDO::getCustomerNo, customerNo)
        ).stream().map(dataObject -> JacksonMapper.convertValue(dataObject, Account.class)).toList();
    }

    public List<Account> search(String customerNo, String accountNo, String accountType) {
        LambdaQueryWrapper<CoreAccountDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(customerNo)) {
            wrapper.eq(CoreAccountDO::getCustomerNo, customerNo);
        }
        if (StringUtils.hasText(accountNo)) {
            wrapper.eq(CoreAccountDO::getAccountNo, accountNo);
        }
        if (StringUtils.hasText(accountType)) {
            wrapper.eq(CoreAccountDO::getAccountType, accountType);
        }
        wrapper.orderByDesc(CoreAccountDO::getId);
        return coreAccountMapper.selectList(wrapper).stream()
                .map(dataObject -> JacksonMapper.convertValue(dataObject, Account.class))
                .toList();
    }

    public void save(Account account) {
        CoreAccountDO dataObject = JacksonMapper.convertValue(account, CoreAccountDO.class);
        coreAccountMapper.insert(dataObject);
    }

    public void updateAvailableBalance(String accountNo, java.math.BigDecimal availableBalance) {
        CoreAccountDO existing = selectByAccountNo(accountNo);
        if (existing == null) {
            return;
        }
        existing.setAvailableBalance(availableBalance);
        coreAccountMapper.updateById(existing);
    }

    private CoreAccountDO selectByAccountNo(String accountNo) {
        return coreAccountMapper.selectOne(
                new LambdaQueryWrapper<CoreAccountDO>().eq(CoreAccountDO::getAccountNo, accountNo),
                false
        );
    }
}
