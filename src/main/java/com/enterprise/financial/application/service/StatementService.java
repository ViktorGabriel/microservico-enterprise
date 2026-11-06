package com.enterprise.financial.application.service;

import com.enterprise.financial.domain.exception.AccountNotFoundException;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.model.Statement;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import com.enterprise.financial.domain.port.TransactionRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class StatementService {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;

    public StatementService(AccountRepositoryPort accountRepository,
                            TransactionRepositoryPort transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Statement generateStatement(UUID accountId, Instant startDate, Instant endDate) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        Instant start = startDate != null ? startDate : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = endDate != null ? endDate : Instant.now();

        List<Transaction> transactions = transactionRepository.findByAccountIdAndPeriod(accountId, start, end);

        BigDecimal totalCredits = BigDecimal.ZERO;
        BigDecimal totalDebits = BigDecimal.ZERO;
        List<Statement.StatementItem> items = new ArrayList<>();

        for (Transaction tx : transactions) {
            boolean isCredit = accountId.equals(tx.getTargetAccountId());
            String entryType = isCredit ? "CREDIT" : "DEBIT";

            if (isCredit) {
                totalCredits = totalCredits.add(tx.getAmount());
            } else {
                totalDebits = totalDebits.add(tx.getAmount());
            }

            items.add(new Statement.StatementItem(
                    tx.getId(),
                    tx.getIdempotencyKey(),
                    tx.getType(),
                    tx.getAmount(),
                    entryType,
                    tx.getDescription(),
                    tx.getCreatedAt()
            ));
        }

        return new Statement(
                account.getId(),
                account.getAccountNumber(),
                account.getHolderName(),
                account.getBalance(),
                start,
                end,
                totalCredits,
                totalDebits,
                items
        );
    }
}
