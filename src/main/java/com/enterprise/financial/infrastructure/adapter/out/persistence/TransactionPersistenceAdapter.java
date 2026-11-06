package com.enterprise.financial.infrastructure.adapter.out.persistence;

import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository repository;

    public TransactionPersistenceAdapter(SpringDataTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionJpaEntity entity = toEntity(transaction);
        TransactionJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return repository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public List<Transaction> findByAccountIdAndPeriod(UUID accountId, Instant start, Instant end) {
        return repository.findByAccountIdAndPeriod(accountId, start, end).stream()
                .map(this::toDomain)
                .toList();
    }

    private TransactionJpaEntity toEntity(Transaction domain) {
        return new TransactionJpaEntity(
                domain.getId(),
                domain.getIdempotencyKey(),
                domain.getSourceAccountId(),
                domain.getTargetAccountId(),
                domain.getAmount(),
                domain.getType(),
                domain.getStatus(),
                domain.getDescription(),
                domain.getCreatedAt()
        );
    }

    private Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getSourceAccountId(),
                entity.getTargetAccountId(),
                entity.getAmount(),
                entity.getType(),
                entity.getStatus(),
                entity.getDescription(),
                entity.getCreatedAt()
        );
    }
}
