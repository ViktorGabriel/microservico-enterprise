package com.enterprise.financial.domain.model;

import com.enterprise.financial.domain.exception.AccountBlockedException;
import com.enterprise.financial.domain.exception.InsufficientBalanceException;
import com.enterprise.financial.domain.exception.InvalidTransactionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Domain Entity Tests")
class AccountTest {

    @Test
    @DisplayName("Should successfully create active account with initial deposit")
    void shouldCreateAccountSuccessfully() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("1000.00"));

        assertNotNull(account.getId());
        assertEquals("123456", account.getAccountNumber());
        assertEquals("12345678901", account.getDocumentNumber());
        assertEquals("Viktor Gabriel", account.getHolderName());
        assertEquals(new BigDecimal("1000.0000"), account.getBalance());
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals(0L, account.getVersion());
    }

    @Test
    @DisplayName("Should credit amount to balance correctly")
    void shouldCreditBalance() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("500.00"));
        account.credit(new BigDecimal("250.50"));

        assertEquals(new BigDecimal("750.5000"), account.getBalance());
    }

    @Test
    @DisplayName("Should debit amount from balance when sufficient funds exist")
    void shouldDebitBalanceWhenSufficientFunds() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("500.00"));
        account.debit(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("300.0000"), account.getBalance());
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when debit exceeds balance")
    void shouldThrowWhenDebitExceedsBalance() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("100.00"));

        assertThrows(InsufficientBalanceException.class, () ->
                account.debit(new BigDecimal("150.00"))
        );
    }

    @Test
    @DisplayName("Should throw AccountBlockedException when transacting on blocked account")
    void shouldThrowWhenAccountBlocked() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("500.00"));
        account.block();

        assertEquals(AccountStatus.BLOCKED, account.getStatus());
        assertThrows(AccountBlockedException.class, () -> account.credit(new BigDecimal("50.00")));
        assertThrows(AccountBlockedException.class, () -> account.debit(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("Should throw InvalidTransactionException when crediting non-positive amount")
    void shouldThrowWhenAmountIsNonPositive() {
        Account account = Account.create("123456", "12345678901", "Viktor Gabriel", new BigDecimal("100.00"));

        assertThrows(InvalidTransactionException.class, () -> account.credit(BigDecimal.ZERO));
        assertThrows(InvalidTransactionException.class, () -> account.credit(new BigDecimal("-10.00")));
        assertThrows(InvalidTransactionException.class, () -> account.debit(new BigDecimal("-5.00")));
    }
}
