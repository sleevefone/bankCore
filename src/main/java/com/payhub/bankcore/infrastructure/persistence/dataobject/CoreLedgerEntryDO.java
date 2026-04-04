package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("core_ledger_entry")
public class CoreLedgerEntryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String coreTxnId;
    private Integer entryNo;
    private String accountNo;
    private Long accountSeqNo;
    private String customerNo;
    private String subjectCode;
    private String entryDirection;
    private String dcDirection;
    private BigDecimal amount;
    private String currency;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCoreTxnId() { return coreTxnId; }
    public void setCoreTxnId(String coreTxnId) { this.coreTxnId = coreTxnId; }
    public Integer getEntryNo() { return entryNo; }
    public void setEntryNo(Integer entryNo) { this.entryNo = entryNo; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public Long getAccountSeqNo() { return accountSeqNo; }
    public void setAccountSeqNo(Long accountSeqNo) { this.accountSeqNo = accountSeqNo; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getEntryDirection() { return entryDirection; }
    public void setEntryDirection(String entryDirection) { this.entryDirection = entryDirection; }
    public String getDcDirection() { return dcDirection; }
    public void setDcDirection(String dcDirection) { this.dcDirection = dcDirection; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
