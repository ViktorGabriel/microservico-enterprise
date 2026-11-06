package com.enterprise.financial.domain.model;

import com.enterprise.financial.domain.exception.AccountBlockedException;
import com.enterprise.financial.domain.exception.InsufficientBalanceException;
import com.enterprise.financial.domain.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final String accountNumber;
    private final String documentNumber;
    private final String holderName;
    private BigDecimal balance;
    private AccountStatus status;
    private Long version;
    private final Instant createdAt;
    private Instant updatedAt;

    public Account(UUID id, String accountNumber, String documentNumber, String holderName,
                   BigDecimal balance, AccountStatus status, Long version,
                   Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "Account ID cannot be null");
        this.accountNumber = Objects.requireNonNull(accountNumber, "Account number cannot be null");
        this.documentNumber = Objects.requireNonNull(documentNumber, "Document number cannot be null");
        this.holderName = Objects.requireNonNull(holderName, "Holder name cannot be null");
        this.balance = normalize(balance != null ? balance : BigDecimal.ZERO);
        this.status = status != null ? status : AccountStatus.ACTIVE;
        this.version = version != null ? version : 0L;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public static Account create(String accountNumber, String documentNumber, String holderName, BigDecimal initialDeposit) {
        Instant now = Instant.now();
        BigDecimal balance = initialDeposit != null ? initialDeposit : BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionException("Depósito inicial não pode ser negativo");
        }
        return new Account(
                UUID.randomUUID(),
                accountNumber,
                documentNumber,
                holderName,
                balance,
                AccountStatus.ACTIVE,
                0L,
                now,
                now
        );
    }

    public void credit(BigDecimal amount) {
        ensureActive();
        validateAmountPositive(amount);
        this.balance = this.balance.add(normalize(amount));
        this.updatedAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        ensureActive();
        validateAmountPositive(amount);
        BigDecimal normalizedAmount = normalize(amount);
        if (this.balance.compareTo(normalizedAmount) < 0) {
            throw new InsufficientBalanceException(this.id, this.balance, normalizedAmount);
        }
        this.balance = this.balance.subtract(normalizedAmount);
        this.updatedAt = Instant.now();
    }

    public void block() {
        this.status = AccountStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    private void ensureActive() {
        if (this.status != AccountStatus.ACTIVE) {
            throw new AccountBlockedException(this.id);
        }
    }

    private void validateAmountPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("O valor da operação deve ser maior que zero");
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        return value.setScale(4, RoundingMode.HALF_EVEN);
    }

    public UUID getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account account)) return false;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
