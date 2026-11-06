package com.enterprise.financial.domain.model;

import com.enterprise.financial.domain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final String idempotencyKey;
    private final UUID sourceAccountId;
    private final UUID targetAccountId;
    private final BigDecimal amount;
    private final TransactionType type;
    private final TransactionStatus status;
    private final String description;
    private final Instant createdAt;

    public Transaction(UUID id, String idempotencyKey, UUID sourceAccountId, UUID targetAccountId,
                       BigDecimal amount, TransactionType type, TransactionStatus status,
                       String description, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "Transaction ID cannot be null");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "Idempotency key cannot be null");
        this.type = Objects.requireNonNull(type, "Transaction type cannot be null");
        this.status = Objects.requireNonNull(status, "Transaction status cannot be null");
        this.amount = validateAmount(amount);
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.description = description;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static Transaction createDeposit(String idempotencyKey, UUID targetAccountId,
                                            BigDecimal amount, String description) {
        if (targetAccountId == null) {
            throw new InvalidTransactionException("Conta de destino é obrigatória para depósito");
        }
        return new Transaction(
                UUID.randomUUID(),
                idempotencyKey,
                null,
                targetAccountId,
                amount,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                description != null ? description : "Depósito em conta",
                Instant.now()
        );
    }

    public static Transaction createWithdrawal(String idempotencyKey, UUID sourceAccountId,
                                               BigDecimal amount, String description) {
        if (sourceAccountId == null) {
            throw new InvalidTransactionException("Conta de origem é obrigatória para saque");
        }
        return new Transaction(
                UUID.randomUUID(),
                idempotencyKey,
                sourceAccountId,
                null,
                amount,
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                description != null ? description : "Saque em conta",
                Instant.now()
        );
    }

    public static Transaction createTransfer(String idempotencyKey, UUID sourceAccountId,
                                             UUID targetAccountId, BigDecimal amount, String description) {
        if (sourceAccountId == null || targetAccountId == null) {
            throw new InvalidTransactionException("Contas de origem e destino são obrigatórias para transferência");
        }
        if (sourceAccountId.equals(targetAccountId)) {
            throw new InvalidTransactionException("Contas de origem e destino não podem ser iguais");
        }
        return new Transaction(
                UUID.randomUUID(),
                idempotencyKey,
                sourceAccountId,
                targetAccountId,
                amount,
                TransactionType.TRANSFER,
                TransactionStatus.COMPLETED,
                description != null ? description : "Transferência entre contas",
                Instant.now()
        );
    }

    private static BigDecimal validateAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Valor da transação deve ser positivo e maior que zero");
        }
        return value.setScale(4, RoundingMode.HALF_EVEN);
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
