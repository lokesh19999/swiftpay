package com.swiftpay.analytics.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentMetric(
        UUID id,
        UUID transactionId,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        String currency,
        Instant processedAt
) {
}
