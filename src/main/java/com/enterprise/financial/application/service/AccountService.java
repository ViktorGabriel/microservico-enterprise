package com.enterprise.financial.application.service;

import com.enterprise.financial.domain.exception.AccountNotFoundException;
import com.enterprise.financial.domain.exception.InvalidTransactionException;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepositoryPort accountRepository;

    public AccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account createAccount(String accountNumber, String documentNumber, String holderName, BigDecimal initialDeposit) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new InvalidTransactionException("Já existe uma conta cadastrada com o número: " + accountNumber);
        }
        if (accountRepository.existsByDocumentNumber(documentNumber)) {
            throw new InvalidTransactionException("Já existe uma conta cadastrada com o documento: " + documentNumber);
        }

        Account account = Account.create(accountNumber, documentNumber, holderName, initialDeposit);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
