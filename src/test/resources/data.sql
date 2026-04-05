DELETE FROM core_audit_log;
DELETE FROM core_transaction_history;
DELETE FROM core_ledger_entry;
DELETE FROM core_transaction;
DELETE FROM core_account;

INSERT INTO core_account (
    account_no, account_seq_no, customer_no, account_type, subject_code, normal_balance_direction,
    owner_id, currency, available_balance, frozen_balance, interest_rate, last_accrual_date, status
) VALUES
    ('ACC-DR-1001', 10001, 'CUST-1001', 'CUSTOMER', '100201', 'DEBIT', 'OWNER-1001', 'CNY', 1000.00, 0.00, 0.03500000, DATE '2026-04-03', 'ACTIVE'),
    ('ACC-CR-2001', 20001, 'CUST-1001', 'MERCHANT', '200101', 'CREDIT', 'OWNER-2001', 'CNY', 500.00, 0.00, 0.02000000, DATE '2026-04-03', 'ACTIVE'),
    ('ACC-DR-2001', 30001, 'CUST-2001', 'CUSTOMER', '100301', 'DEBIT', 'OWNER-3001', 'CNY', 600.00, 0.00, 0.03000000, DATE '2026-04-03', 'ACTIVE'),
    ('ACC-CR-3001', 30002, 'CUST-2001', 'MERCHANT', '200301', 'CREDIT', 'OWNER-3002', 'CNY', 900.00, 0.00, 0.02500000, DATE '2026-04-03', 'ACTIVE');
