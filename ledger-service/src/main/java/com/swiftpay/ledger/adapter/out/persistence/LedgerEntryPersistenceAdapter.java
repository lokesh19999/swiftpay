package com.swiftpay.ledger.adapter.out.persistence;

import com.swiftpay.ledger.adapter.out.persistence.entity.LedgerEntryEntity;
import com.swiftpay.ledger.adapter.out.persistence.repository.LedgerEntryJpaRepository;
import com.swiftpay.ledger.domain.model.LedgerEntry;
import com.swiftpay.ledger.domain.port.LedgerEntryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LedgerEntryPersistenceAdapter implements LedgerEntryRepositoryPort {

    private final LedgerEntryJpaRepository repository;

    @Override
    public boolean existsByTransactionId(UUID transactionId) {
        return repository.existsByTransactionId(transactionId);
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(entry.id());
        entity.setTransactionId(entry.transactionId());
        entity.setAccountId(entry.accountId());
        entity.setEntryType(entry.type());
        entity.setAmount(entry.amount());
        entity.setCurrency(entry.currency());
        entity.setCreatedAt(entry.createdAt());
        return toDomain(repository.save(entity));
    }

    private LedgerEntry toDomain(LedgerEntryEntity entity) {
        return new LedgerEntry(
                entity.getId(),
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getEntryType(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getCreatedAt()
        );
    }
}
