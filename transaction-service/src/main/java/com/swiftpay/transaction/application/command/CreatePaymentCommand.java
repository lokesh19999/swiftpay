package com.swiftpay.transaction.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentCommand(
        UUID idempotencyKey,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        String currency
) {
}
