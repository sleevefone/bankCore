package com.payhub.bankcore.infrastructure.persistence.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("core_transaction")
public class CoreTransactionDO {
    @TableId
    private String coreTxnId;
    private String requestId;
    private String bizOrderId;
    private String bizType;
    private String txnType;
    private String customerNo;
    private BigDecimal amount;
    private String currency;
    private String debitAccountNo;
    private Long debitAccountSeqNo;
    private String debitSubjectCode;
    private String creditAccountNo;
    private Long creditAccountSeqNo;
    private String creditSubjectCode;
    private String status;
    private String failureCode;
    private String failureMessage;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getCoreTxnId() { return coreTxnId; }
    public void setCoreTxnId(String coreTxnId) { this.coreTxnId = coreTxnId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getBizOrderId() { return bizOrderId; }
    public void setBizOrderId(String bizOrderId) { this.bizOrderId = bizOrderId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getTxnType() { return txnType; }
    public void setTxnType(String txnType) { this.txnType = txnType; }
    public String getCustomerNo() { return customerNo; }
    public void setCustomerNo(String customerNo) { this.customerNo = customerNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDebitAccountNo() { return debitAccountNo; }
    public void setDebitAccountNo(String debitAccountNo) { this.debitAccountNo = debitAccountNo; }
    public Long getDebitAccountSeqNo() { return debitAccountSeqNo; }
    public void setDebitAccountSeqNo(Long debitAccountSeqNo) { this.debitAccountSeqNo = debitAccountSeqNo; }
    public String getDebitSubjectCode() { return debitSubjectCode; }
    public void setDebitSubjectCode(String debitSubjectCode) { this.debitSubjectCode = debitSubjectCode; }
    public String getCreditAccountNo() { return creditAccountNo; }
    public void setCreditAccountNo(String creditAccountNo) { this.creditAccountNo = creditAccountNo; }
    public Long getCreditAccountSeqNo() { return creditAccountSeqNo; }
    public void setCreditAccountSeqNo(Long creditAccountSeqNo) { this.creditAccountSeqNo = creditAccountSeqNo; }
    public String getCreditSubjectCode() { return creditSubjectCode; }
    public void setCreditSubjectCode(String creditSubjectCode) { this.creditSubjectCode = creditSubjectCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
