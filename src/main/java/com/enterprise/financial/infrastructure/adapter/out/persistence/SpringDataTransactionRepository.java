package com.enterprise.financial.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTransactionRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    Optional<TransactionJpaEntity> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    @Query("""
        SELECT t FROM TransactionJpaEntity t
        WHERE (t.sourceAccountId = :accountId OR t.targetAccountId = :accountId)
          AND t.createdAt >= :start AND t.createdAt <= :end
        ORDER BY t.createdAt DESC
    """)
    List<TransactionJpaEntity> findByAccountIdAndPeriod(@Param("accountId") UUID accountId,
                                                        @Param("start") Instant start,
                                                        @Param("end") Instant end);
}
