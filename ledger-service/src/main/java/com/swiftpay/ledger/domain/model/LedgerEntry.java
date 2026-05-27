package com.swiftpay.ledger.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID transactionId,
        UUID accountId,
        EntryType type,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
    public enum EntryType {
        DEBIT, CREDIT
    }
}
