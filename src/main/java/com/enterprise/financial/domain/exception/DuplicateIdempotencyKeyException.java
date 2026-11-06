package com.enterprise.financial.domain.exception;

public class DuplicateIdempotencyKeyException extends BusinessException {
    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("Transação com chave de idempotência '" + idempotencyKey + "' já foi processada anteriormente.");
    }
}
