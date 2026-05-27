package com.swiftpay.ledger.domain.port;

import com.swiftpay.ledger.domain.model.LedgerEntry;

import java.util.UUID;

public interface LedgerEntryRepositoryPort {

    boolean existsByTransactionId(UUID transactionId);

    LedgerEntry save(LedgerEntry entry);
}
