package com.swiftpay.ledger.domain.exception;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    private final UUID accountId;

    public InsufficientBalanceException(UUID accountId) {
        super("Insufficient balance for account: " + accountId);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
