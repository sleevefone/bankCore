package com.payhub.bankcore.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreCustomerDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreCustomerMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class CoreCustomerRepository {

    private final CoreCustomerMapper coreCustomerMapper;

    public CoreCustomerRepository(CoreCustomerMapper coreCustomerMapper) {
        this.coreCustomerMapper = coreCustomerMapper;
    }

    public Optional<CoreCustomerDO> findByCustomerNo(String customerNo) {
        return Optional.ofNullable(coreCustomerMapper.selectOne(
                new LambdaQueryWrapper<CoreCustomerDO>().eq(CoreCustomerDO::getCustomerNo, customerNo),
                false
        ));
    }

    public void save(CoreCustomerDO customer) {
        coreCustomerMapper.insert(customer);
    }

    public List<CoreCustomerDO> search(String customerNo, String customerName, String mobile) {
        LambdaQueryWrapper<CoreCustomerDO> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(customerNo)) {
            wrapper.eq(CoreCustomerDO::getCustomerNo, customerNo);
        }
        if (StringUtils.hasText(customerName)) {
            wrapper.like(CoreCustomerDO::getCustomerName, customerName);
        }
        if (StringUtils.hasText(mobile)) {
            wrapper.eq(CoreCustomerDO::getMobile, mobile);
        }
        wrapper.orderByDesc(CoreCustomerDO::getId);
        return coreCustomerMapper.selectList(wrapper);
    }
}
