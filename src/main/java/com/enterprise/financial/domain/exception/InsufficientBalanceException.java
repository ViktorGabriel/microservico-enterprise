package com.enterprise.financial.domain.exception;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends BusinessException {
    public InsufficientBalanceException(UUID accountId, BigDecimal balance, BigDecimal requestedAmount) {
        super(String.format("Saldo insuficiente na conta %s. Saldo disponível: %s, Solicitado: %s",
                accountId, balance, requestedAmount));
    }
}
