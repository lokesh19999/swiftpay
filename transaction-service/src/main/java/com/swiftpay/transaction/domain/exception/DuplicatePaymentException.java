package com.swiftpay.transaction.domain.exception;

import java.util.UUID;

public class DuplicatePaymentException extends RuntimeException {

    private final UUID idempotencyKey;

    public DuplicatePaymentException(UUID idempotencyKey) {
        super("Duplicate idempotency key: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
