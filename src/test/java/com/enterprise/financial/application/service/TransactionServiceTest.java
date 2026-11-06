package com.enterprise.financial.application.service;

import com.enterprise.financial.domain.exception.AccountNotFoundException;
import com.enterprise.financial.domain.exception.InsufficientBalanceException;
import com.enterprise.financial.domain.exception.InvalidTransactionException;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.model.TransactionStatus;
import com.enterprise.financial.domain.model.TransactionType;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import com.enterprise.financial.domain.port.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transaction Service Unit Tests")
class TransactionServiceTest {

    @Mock
    private AccountRepositoryPort accountRepository;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = Account.create("1001", "11111111111", "Alice", new BigDecimal("1000.00"));
        targetAccount = Account.create("2002", "22222222222", "Bob", new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should return existing transaction when idempotency key is already recorded")
    void shouldReturnExistingWhenIdempotencyKeyFound() {
        String key = "idempotency-key-123";
        Transaction existing = Transaction.createDeposit(key, targetAccount.getId(), new BigDecimal("100.00"), "Deposit");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        Transaction result = transactionService.processDeposit(key, targetAccount.getId(), new BigDecimal("100.00"), "Deposit");

        assertSame(existing, result);
        verify(accountRepository, never()).findByIdWithLock(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully process deposit")
    void shouldProcessDepositSuccessfully() {
        String key = "dep-1";
        BigDecimal amount = new BigDecimal("200.00");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(accountRepository.findByIdWithLock(targetAccount.getId())).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.processDeposit(key, targetAccount.getId(), amount, "Salário");

        assertNotNull(result);
        assertEquals(TransactionType.DEPOSIT, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("700.0000"), targetAccount.getBalance());
        verify(accountRepository).save(targetAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should successfully process withdrawal")
    void shouldProcessWithdrawalSuccessfully() {
        String key = "wd-1";
        BigDecimal amount = new BigDecimal("300.00");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(accountRepository.findByIdWithLock(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.processWithdrawal(key, sourceAccount.getId(), amount, "Saque ATM");

        assertNotNull(result);
        assertEquals(TransactionType.WITHDRAWAL, result.getType());
        assertEquals(TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(new BigDecimal("700.0000"), sourceAccount.getBalance());
    }

    @Test
    @DisplayName("Should successfully process transfer between accounts")
    void shouldProcessTransferSuccessfully() {
        String key = "tr-1";
        BigDecimal amount = new BigDecimal("400.00");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(accountRepository.findByIdWithLock(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(targetAccount.getId())).thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.processTransfer(key, sourceAccount.getId(), targetAccount.getId(), amount, "PIX");

        assertNotNull(result);
        assertEquals(TransactionType.TRANSFER, result.getType());
        assertEquals(new BigDecimal("600.0000"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("900.0000"), targetAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw InvalidTransactionException when transferring to the same account")
    void shouldThrowWhenTransferringToSameAccount() {
        String key = "tr-same";
        BigDecimal amount = new BigDecimal("100.00");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());

        assertThrows(InvalidTransactionException.class, () ->
                transactionService.processTransfer(key, sourceAccount.getId(), sourceAccount.getId(), amount, "Invalid")
        );
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException during transfer if source balance is low")
    void shouldThrowWhenTransferExceedsBalance() {
        String key = "tr-fail";
        BigDecimal amount = new BigDecimal("5000.00");

        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(accountRepository.findByIdWithLock(sourceAccount.getId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdWithLock(targetAccount.getId())).thenReturn(Optional.of(targetAccount));

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.processTransfer(key, sourceAccount.getId(), targetAccount.getId(), amount, "PIX")
        );
    }
}
