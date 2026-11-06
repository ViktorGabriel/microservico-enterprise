package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.model.TransactionStatus;
import com.enterprise.financial.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        String idempotencyKey,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        String description,
        Instant createdAt
) {
    public static TransactionResponse fromDomain(Transaction tx) {
        return new TransactionResponse(
                tx.getId(),
                tx.getIdempotencyKey(),
                tx.getSourceAccountId(),
                tx.getTargetAccountId(),
                tx.getAmount(),
                tx.getType(),
                tx.getStatus(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }
}
