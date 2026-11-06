package com.enterprise.financial.domain.exception;

public class InvalidTransactionException extends BusinessException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
