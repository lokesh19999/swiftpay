package com.swiftpay.transaction.domain.model;

import com.swiftpay.shared.domain.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        UUID idempotencyKey,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {

    public Transaction withStatus(TransactionStatus newStatus, String reason) {
        return new Transaction(
                id, idempotencyKey, senderAccountId, receiverAccountId,
                amount, currency, newStatus, reason, createdAt, Instant.now()
        );
    }
}
