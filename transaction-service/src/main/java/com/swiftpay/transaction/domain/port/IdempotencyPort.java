package com.swiftpay.transaction.domain.port;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyPort {

    /**
     * @return existing transaction id if key was already processed
     */
    Optional<UUID> findExistingTransactionId(UUID idempotencyKey);

    /**
     * Registers idempotency key; returns false if key already exists (race).
     */
    boolean tryRegister(UUID idempotencyKey, UUID transactionId);
}
