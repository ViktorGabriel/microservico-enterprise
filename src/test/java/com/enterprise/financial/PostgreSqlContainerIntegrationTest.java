package com.enterprise.financial;

import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.domain.model.TransactionType;
import com.enterprise.financial.domain.port.AccountRepositoryPort;
import com.enterprise.financial.domain.port.TransactionRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Real PostgreSQL Testcontainers Integration Test")
class PostgreSqlContainerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("financial_integration_db")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        try {
            if (DockerClientFactory.instance().isDockerAvailable()) {
                registry.add("spring.datasource.url", postgres::getJdbcUrl);
                registry.add("spring.datasource.username", postgres::getUsername);
                registry.add("spring.datasource.password", postgres::getPassword);
                registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
                registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
                registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
                registry.add("spring.flyway.enabled", () -> "true");
            }
        } catch (Exception ignored) {
        }
    }

    @Autowired(required = false)
    private AccountRepositoryPort accountRepository;

    @Autowired(required = false)
    private TransactionRepositoryPort transactionRepository;

    @Test
    @DisplayName("Should execute real database migrations and persist accounts & transactions in PostgreSQL")
    void shouldPersistAndQueryInRealPostgreSQL() {
        assertNotNull(accountRepository, "AccountRepositoryPort should be injected");
        assertNotNull(transactionRepository, "TransactionRepositoryPort should be injected");

        // 1. Create and persist Account in real PostgreSQL
        Account account = Account.create("PG-99001", "98765432100", "PostgreSQL Holder", new BigDecimal("5000.00"));
        Account savedAccount = accountRepository.save(account);

        assertNotNull(savedAccount.getId());
        assertEquals("PG-99001", savedAccount.getAccountNumber());

        // 2. Query Account back
        Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());
        assertTrue(foundAccount.isPresent());
        assertEquals(new BigDecimal("5000.0000"), foundAccount.get().getBalance());

        // 3. Persist real transaction
        String key = "testcontainer-idempotency-" + UUID.randomUUID();
        Transaction tx = Transaction.createDeposit(key, savedAccount.getId(), new BigDecimal("1500.00"), "Depósito Real");
        Transaction savedTx = transactionRepository.save(tx);

        assertNotNull(savedTx.getId());
        assertEquals(key, savedTx.getIdempotencyKey());

        // 4. Query transaction by period
        List<Transaction> transactions = transactionRepository.findByAccountIdAndPeriod(
                savedAccount.getId(),
                Instant.now().minusSeconds(3600),
                Instant.now().plusSeconds(3600)
        );

        assertFalse(transactions.isEmpty());
        assertEquals(TransactionType.DEPOSIT, transactions.getFirst().getType());
    }
}
