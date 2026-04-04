ALTER TABLE core_account
    ADD COLUMN account_seq_no BIGINT NULL COMMENT 'Internal account sequence number' AFTER account_no,
    ADD COLUMN customer_no VARCHAR(64) NULL COMMENT 'Customer number' AFTER account_seq_no,
    ADD COLUMN subject_code VARCHAR(64) NULL COMMENT 'Default subject code' AFTER account_type,
    ADD COLUMN normal_balance_direction VARCHAR(8) NULL COMMENT 'Normal balance direction: DEBIT/CREDIT' AFTER subject_code,
    ADD COLUMN interest_rate DECIMAL(12, 8) NULL COMMENT 'Default interest rate' AFTER frozen_balance,
    ADD COLUMN last_accrual_date DATE NULL COMMENT 'Last accrual business date' AFTER interest_rate;

ALTER TABLE core_account
    ADD CONSTRAINT uk_core_account_seq_no UNIQUE (account_seq_no);

ALTER TABLE core_account
    ADD INDEX idx_core_account_customer_no (customer_no),
    ADD INDEX idx_core_account_subject_code (subject_code);

CREATE TABLE core_subject (
    subject_code VARCHAR(64) PRIMARY KEY COMMENT 'Subject code',
    subject_name VARCHAR(128) NOT NULL COMMENT 'Subject name',
    subject_level INT NOT NULL COMMENT 'Subject hierarchy level',
    parent_subject_code VARCHAR(64) NULL COMMENT 'Parent subject code',
    normal_balance_direction VARCHAR(8) NOT NULL COMMENT 'Normal balance direction: DEBIT/CREDIT',
    interest_bearing TINYINT(1) NOT NULL DEFAULT 0 COMMENT 'Whether the subject bears interest',
    status VARCHAR(32) NOT NULL COMMENT 'Subject status',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX idx_core_subject_parent_code (parent_subject_code)
) COMMENT='Accounting subject master data';

CREATE TABLE core_transaction (
    core_txn_id VARCHAR(64) PRIMARY KEY COMMENT 'Core transaction id',
    request_id VARCHAR(64) NOT NULL COMMENT 'Idempotent request id',
    biz_order_id VARCHAR(64) NOT NULL COMMENT 'Business order id',
    biz_type VARCHAR(64) NOT NULL COMMENT 'Business type',
    txn_type VARCHAR(64) NOT NULL COMMENT 'Transaction type',
    amount DECIMAL(18, 2) NOT NULL COMMENT 'Transaction amount',
    currency VARCHAR(16) NOT NULL COMMENT 'Currency code',
    debit_account_no VARCHAR(64) NOT NULL COMMENT 'Debit account number',
    credit_account_no VARCHAR(64) NOT NULL COMMENT 'Credit account number',
    status VARCHAR(32) NOT NULL COMMENT 'Transaction status',
    failure_code VARCHAR(64) NULL COMMENT 'Failure code',
    failure_message VARCHAR(255) NULL COMMENT 'Failure message',
    occurred_at DATETIME NOT NULL COMMENT 'Business occurrence time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    CONSTRAINT uk_core_transaction_request_id UNIQUE (request_id),
    INDEX idx_core_transaction_biz_order_id (biz_order_id),
    INDEX idx_core_transaction_status (status)
) COMMENT='Core transaction master table';

CREATE TABLE core_ledger_entry (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    core_txn_id VARCHAR(64) NOT NULL COMMENT 'Core transaction id',
    entry_no INT NOT NULL COMMENT 'Entry number in one transaction',
    account_no VARCHAR(64) NOT NULL COMMENT 'Account number',
    account_seq_no BIGINT NULL COMMENT 'Internal account sequence number',
    customer_no VARCHAR(64) NULL COMMENT 'Customer number',
    subject_code VARCHAR(64) NOT NULL COMMENT 'Subject code',
    entry_direction VARCHAR(32) NOT NULL COMMENT 'Business entry direction',
    dc_direction VARCHAR(8) NOT NULL COMMENT 'Debit or credit direction',
    amount DECIMAL(18, 2) NOT NULL COMMENT 'Entry amount',
    currency VARCHAR(16) NOT NULL COMMENT 'Currency code',
    balance_before DECIMAL(18, 2) NOT NULL COMMENT 'Balance before posting',
    balance_after DECIMAL(18, 2) NOT NULL COMMENT 'Balance after posting',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    CONSTRAINT uk_core_ledger_entry_txn_entry UNIQUE (core_txn_id, entry_no),
    INDEX idx_core_ledger_entry_account_no (account_no),
    INDEX idx_core_ledger_entry_customer_no (customer_no),
    INDEX idx_core_ledger_entry_subject_code (subject_code)
) COMMENT='Ledger entries with audit dimensions';

CREATE TABLE core_interest_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    account_no VARCHAR(64) NOT NULL COMMENT 'Account number',
    account_seq_no BIGINT NULL COMMENT 'Internal account sequence number',
    customer_no VARCHAR(64) NULL COMMENT 'Customer number',
    subject_code VARCHAR(64) NOT NULL COMMENT 'Subject code',
    interest_rate DECIMAL(12, 8) NOT NULL COMMENT 'Interest rate',
    interest_base_amount DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Interest bearing principal',
    accrued_interest DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Accrued interest amount',
    settled_interest DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Settled interest amount',
    last_accrual_date DATE NULL COMMENT 'Last accrual business date',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX idx_core_interest_detail_account_no (account_no),
    INDEX idx_core_interest_detail_customer_no (customer_no)
) COMMENT='Interest detail by account and subject';

CREATE TABLE core_accrual_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    business_date DATE NOT NULL COMMENT 'Business date',
    account_no VARCHAR(64) NOT NULL COMMENT 'Account number',
    account_seq_no BIGINT NULL COMMENT 'Internal account sequence number',
    customer_no VARCHAR(64) NULL COMMENT 'Customer number',
    subject_code VARCHAR(64) NOT NULL COMMENT 'Subject code',
    accrual_type VARCHAR(32) NOT NULL COMMENT 'Accrual type',
    dc_direction VARCHAR(8) NOT NULL COMMENT 'Debit or credit direction',
    accrual_amount DECIMAL(18, 2) NOT NULL COMMENT 'Accrual amount',
    status VARCHAR(32) NOT NULL COMMENT 'Accrual status',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    INDEX idx_core_accrual_record_business_date (business_date),
    INDEX idx_core_accrual_record_account_no (account_no),
    INDEX idx_core_accrual_record_customer_no (customer_no)
) COMMENT='Accrual records for interest and fee recognition';

CREATE TABLE core_transaction_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    core_txn_id VARCHAR(64) NOT NULL COMMENT 'Core transaction id',
    before_status VARCHAR(32) NULL COMMENT 'Status before transition',
    after_status VARCHAR(32) NOT NULL COMMENT 'Status after transition',
    event_type VARCHAR(64) NOT NULL COMMENT 'Event type',
    message VARCHAR(255) NULL COMMENT 'Transition message',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    INDEX idx_core_transaction_history_txn_id (core_txn_id),
    INDEX idx_core_transaction_history_created_at (created_at)
) COMMENT='Core transaction status history';

CREATE TABLE core_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    entity_type VARCHAR(64) NOT NULL COMMENT 'Entity type',
    entity_id VARCHAR(64) NOT NULL COMMENT 'Entity id',
    operation_type VARCHAR(64) NOT NULL COMMENT 'Operation type',
    operator_id VARCHAR(64) NULL COMMENT 'Operator id',
    trace_id VARCHAR(64) NULL COMMENT 'Trace id',
    before_snapshot TEXT NULL COMMENT 'State before change',
    after_snapshot TEXT NULL COMMENT 'State after change',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    INDEX idx_core_audit_log_entity (entity_type, entity_id),
    INDEX idx_core_audit_log_trace_id (trace_id),
    INDEX idx_core_audit_log_created_at (created_at)
) COMMENT='Unified audit log';
