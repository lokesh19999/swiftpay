package com.swiftpay.transaction.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payment initiation request")
public record PaymentRequestDto(
        @NotNull
        @Schema(description = "Client-supplied idempotency key (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID idempotencyKey,

        @NotNull
        @Schema(description = "Sender account UUID")
        UUID senderAccountId,

        @NotNull
        @Schema(description = "Receiver account UUID")
        UUID receiverAccountId,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        @Schema(example = "100.50")
        BigDecimal amount,

        @NotBlank
        @Size(min = 3, max = 3)
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must be ISO 4217 uppercase")
        @Schema(example = "USD")
        String currency
) {
}
