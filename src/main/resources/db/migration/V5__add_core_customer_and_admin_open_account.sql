CREATE TABLE IF NOT EXISTS core_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    customer_no VARCHAR(64) NOT NULL COMMENT 'Customer number',
    customer_name VARCHAR(128) NOT NULL COMMENT 'Customer name',
    mobile VARCHAR(32) NULL COMMENT 'Mobile number',
    id_no VARCHAR(64) NULL COMMENT 'Identity number',
    status VARCHAR(32) NOT NULL COMMENT 'Customer status',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    CONSTRAINT uk_core_customer_customer_no UNIQUE (customer_no),
    INDEX idx_core_customer_mobile (mobile)
) COMMENT='Core customer master data';
