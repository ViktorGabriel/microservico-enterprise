package com.enterprise.financial.domain.port;

import com.enterprise.financial.domain.model.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);
    List<Transaction> findByAccountIdAndPeriod(UUID accountId, Instant start, Instant end);
}
