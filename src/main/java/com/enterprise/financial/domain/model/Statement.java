package com.enterprise.financial.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Statement(
        UUID accountId,
        String accountNumber,
        String holderName,
        BigDecimal currentBalance,
        Instant periodStart,
        Instant periodEnd,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        List<StatementItem> items
) {
    public record StatementItem(
            UUID transactionId,
            String idempotencyKey,
            TransactionType type,
            BigDecimal amount,
            String entryType, // CREDIT or DEBIT from the perspective of this account
            String description,
            Instant timestamp
    ) {}
}
