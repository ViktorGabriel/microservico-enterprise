package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import com.enterprise.financial.domain.model.Statement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record StatementResponse(
        UUID accountId,
        String accountNumber,
        String holderName,
        BigDecimal currentBalance,
        Instant periodStart,
        Instant periodEnd,
        BigDecimal totalCredits,
        BigDecimal totalDebits,
        List<Statement.StatementItem> items
) {
    public static StatementResponse fromDomain(Statement statement) {
        return new StatementResponse(
                statement.accountId(),
                statement.accountNumber(),
                statement.holderName(),
                statement.currentBalance(),
                statement.periodStart(),
                statement.periodEnd(),
                statement.totalCredits(),
                statement.totalDebits(),
                statement.items()
        );
    }
}
