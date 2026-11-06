package com.enterprise.financial.infrastructure.adapter.out.persistence;

import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AccountPersistenceAdapter implements AccountRepositoryPort {

    private final SpringDataAccountRepository repository;

    public AccountPersistenceAdapter(SpringDataAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Account save(Account account) {
        AccountJpaEntity entity = toEntity(account);
        AccountJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByIdWithLock(UUID id) {
        return repository.findByIdWithPessimisticLock(id).map(this::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return repository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return repository.existsByAccountNumber(accountNumber);
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return repository.existsByDocumentNumber(documentNumber);
    }

    private AccountJpaEntity toEntity(Account domain) {
        return new AccountJpaEntity(
                domain.getId(),
                domain.getAccountNumber(),
                domain.getDocumentNumber(),
                domain.getHolderName(),
                domain.getBalance(),
                domain.getStatus(),
                domain.getVersion(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    private Account toDomain(AccountJpaEntity entity) {
        return new Account(
                entity.getId(),
                entity.getAccountNumber(),
                entity.getDocumentNumber(),
                entity.getHolderName(),
                entity.getBalance(),
                entity.getStatus(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
