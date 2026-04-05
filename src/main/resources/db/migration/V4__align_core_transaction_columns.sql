ALTER TABLE core_transaction
    ADD COLUMN customer_no VARCHAR(64) NULL COMMENT 'Customer number' AFTER txn_type,
    ADD COLUMN debit_account_seq_no BIGINT NULL COMMENT 'Debit account sequence number' AFTER debit_account_no,
    ADD COLUMN debit_subject_code VARCHAR(64) NULL COMMENT 'Debit subject code' AFTER debit_account_seq_no,
    ADD COLUMN credit_account_seq_no BIGINT NULL COMMENT 'Credit account sequence number' AFTER credit_account_no,
    ADD COLUMN credit_subject_code VARCHAR(64) NULL COMMENT 'Credit subject code' AFTER credit_account_seq_no;
