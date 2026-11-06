package com.enterprise.financial.domain.port;

import com.enterprise.financial.domain.model.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    Optional<Account> findByIdWithLock(UUID id);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByDocumentNumber(String documentNumber);
}
