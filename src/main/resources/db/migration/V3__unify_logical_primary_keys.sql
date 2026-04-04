ALTER TABLE core_account
    DROP PRIMARY KEY;

ALTER TABLE core_account
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD CONSTRAINT uk_core_account_account_no UNIQUE (account_no);

ALTER TABLE core_subject
    DROP PRIMARY KEY;

ALTER TABLE core_subject
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD CONSTRAINT uk_core_subject_subject_code UNIQUE (subject_code);

ALTER TABLE core_transaction
    DROP PRIMARY KEY;

ALTER TABLE core_transaction
    ADD COLUMN id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST,
    ADD CONSTRAINT uk_core_transaction_core_txn_id UNIQUE (core_txn_id);
