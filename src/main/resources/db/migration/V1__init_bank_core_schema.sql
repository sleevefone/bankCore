CREATE TABLE core_account (
    account_no VARCHAR(64) PRIMARY KEY COMMENT 'Account number',
    account_type VARCHAR(32) NOT NULL COMMENT 'Account type',
    owner_id VARCHAR(64) NOT NULL COMMENT 'Owner identity',
    currency VARCHAR(16) NOT NULL COMMENT 'Currency code',
    available_balance DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Available balance',
    frozen_balance DECIMAL(18, 2) NOT NULL DEFAULT 0 COMMENT 'Frozen balance',
    status VARCHAR(32) NOT NULL COMMENT 'Account status',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time'
) COMMENT='Core account table';
