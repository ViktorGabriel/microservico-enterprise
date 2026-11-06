package com.enterprise.financial.application.service;

import com.enterprise.financial.domain.exception.AccountNotFoundException;
import com.enterprise.financial.domain.exception.DuplicateIdempotencyKeyException;
import com.enterprise.financial.domain.exception.InvalidTransactionException;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import com.enterprise.financial.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;

    public TransactionService(AccountRepositoryPort accountRepository,
                              TransactionRepositoryPort transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction processDeposit(String idempotencyKey, UUID targetAccountId,
                                      BigDecimal amount, String description) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        Account account = accountRepository.findByIdWithLock(targetAccountId)
                .orElseThrow(() -> new AccountNotFoundException(targetAccountId));

        account.credit(amount);
        accountRepository.save(account);

        Transaction transaction = Transaction.createDeposit(idempotencyKey, targetAccountId, amount, description);
        return transactionRepository.save(transaction);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction processWithdrawal(String idempotencyKey, UUID sourceAccountId,
                                         BigDecimal amount, String description) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        Account account = accountRepository.findByIdWithLock(sourceAccountId)
                .orElseThrow(() -> new AccountNotFoundException(sourceAccountId));

        account.debit(amount);
        accountRepository.save(account);

        Transaction transaction = Transaction.createWithdrawal(idempotencyKey, sourceAccountId, amount, description);
        return transactionRepository.save(transaction);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Transaction processTransfer(String idempotencyKey, UUID sourceAccountId,
                                       UUID targetAccountId, BigDecimal amount, String description) {
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (sourceAccountId.equals(targetAccountId)) {
            throw new InvalidTransactionException("Transferência não pode ser realizada para a mesma conta");
        }

        // Deadlock Prevention: Deterministic Lock Acquisition Order by UUID
        Account firstLocked;
        Account secondLocked;
        if (sourceAccountId.compareTo(targetAccountId) < 0) {
            firstLocked = accountRepository.findByIdWithLock(sourceAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(sourceAccountId));
            secondLocked = accountRepository.findByIdWithLock(targetAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(targetAccountId));
        } else {
            firstLocked = accountRepository.findByIdWithLock(targetAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(targetAccountId));
            secondLocked = accountRepository.findByIdWithLock(sourceAccountId)
                    .orElseThrow(() -> new AccountNotFoundException(sourceAccountId));
        }

        Account source = sourceAccountId.equals(firstLocked.getId()) ? firstLocked : secondLocked;
        Account target = targetAccountId.equals(firstLocked.getId()) ? firstLocked : secondLocked;

        source.debit(amount);
        target.credit(amount);

        accountRepository.save(source);
        accountRepository.save(target);

        Transaction transaction = Transaction.createTransfer(idempotencyKey, sourceAccountId, targetAccountId, amount, description);
        return transactionRepository.save(transaction);
    }
}
