package com.enterprise.financial.domain.exception;

import java.util.UUID;

public class AccountBlockedException extends BusinessException {
    public AccountBlockedException(UUID accountId) {
        super("A conta " + accountId + " está bloqueada ou inativa para transações.");
    }
}
