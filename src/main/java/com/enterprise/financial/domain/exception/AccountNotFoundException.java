package com.enterprise.financial.domain.exception;

import java.util.UUID;

public class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(UUID accountId) {
        super("Conta não encontrada para o identificador: " + accountId);
    }

    public AccountNotFoundException(String accountNumber) {
        super("Conta não encontrada para o número: " + accountNumber);
    }
}
