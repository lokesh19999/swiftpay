package com.swiftpay.ledger.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Account(
        UUID id,
        String ownerName,
        BigDecimal balance,
        String currency
) {
}
