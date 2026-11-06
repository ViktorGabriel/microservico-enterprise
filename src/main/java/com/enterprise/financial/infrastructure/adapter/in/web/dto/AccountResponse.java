package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.domain.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String documentNumber,
        String holderName,
        BigDecimal balance,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static AccountResponse fromDomain(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getDocumentNumber(),
                account.getHolderName(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
