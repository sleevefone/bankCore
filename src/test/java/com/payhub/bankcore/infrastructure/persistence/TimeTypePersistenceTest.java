package com.payhub.bankcore.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreAccountDO;
import com.payhub.bankcore.infrastructure.persistence.dataobject.CoreTransactionDO;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreAccountMapper;
import com.payhub.bankcore.infrastructure.persistence.mapper.CoreTransactionMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TimeTypePersistenceTest {

    @Autowired
    private CoreAccountMapper coreAccountMapper;

    @Autowired
    private CoreTransactionMapper coreTransactionMapper;

    @Test
    void shouldReadLocalDateFromAccount() {
        CoreAccountDO account = coreAccountMapper.selectOne(
                new LambdaQueryWrapper<CoreAccountDO>().eq(CoreAccountDO::getAccountNo, "ACC-DR-1001"),
                false
        );

        assertNotNull(account);
        assertEquals(LocalDate.of(2026, 4, 3), account.getLastAccrualDate());
    }

    @Test
    void shouldPersistAndReadLocalDateTimeFromCoreTransaction() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 4, 20, 15, 30);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 4, 20, 16, 0);

        CoreTransactionDO transaction = new CoreTransactionDO();
        transaction.setCoreTxnId("CTX-TIME-1001");
        transaction.setRequestId("REQ-TIME-1001");
        transaction.setBizOrderId("BIZ-TIME-1001");
        transaction.setBizType("PAYMENT");
        transaction.setTxnType("PAY_IN");
        transaction.setCustomerNo("CUST-1001");
        transaction.setAmount(new BigDecimal("12.34"));
        transaction.setCurrency("CNY");
        transaction.setDebitAccountNo("ACC-DR-1001");
        transaction.setDebitAccountSeqNo(10001L);
        transaction.setDebitSubjectCode("100201");
        transaction.setCreditAccountNo("ACC-CR-2001");
        transaction.setCreditAccountSeqNo(20001L);
        transaction.setCreditSubjectCode("200101");
        transaction.setStatus("SUCCESS");
        transaction.setOccurredAt(occurredAt);
        transaction.setCreatedAt(createdAt);
        transaction.setUpdatedAt(createdAt);

        coreTransactionMapper.insert(transaction);

        CoreTransactionDO loaded = coreTransactionMapper.selectOne(
                new LambdaQueryWrapper<CoreTransactionDO>().eq(CoreTransactionDO::getCoreTxnId, "CTX-TIME-1001"),
                false
        );
        assertNotNull(loaded);
        assertEquals(occurredAt, loaded.getOccurredAt());
        assertEquals(createdAt, loaded.getCreatedAt());
    }
}
