CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    source_account_id UUID REFERENCES accounts(id),
    target_account_id UUID REFERENCES accounts(id),
    amount NUMERIC(19, 4) NOT NULL,
    type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key);
CREATE INDEX idx_transactions_source_acc ON transactions(source_account_id);
CREATE INDEX idx_transactions_target_acc ON transactions(target_account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
