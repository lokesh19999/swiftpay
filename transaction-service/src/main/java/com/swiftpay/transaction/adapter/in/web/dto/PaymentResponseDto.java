package com.swiftpay.transaction.adapter.in.web.dto;

import com.swiftpay.shared.domain.TransactionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Payment initiation response")
public record PaymentResponseDto(
        UUID transactionId,
        UUID idempotencyKey,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        String currency,
        TransactionStatus status,
        Instant createdAt
) {
}
