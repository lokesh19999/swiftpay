package com.swiftpay.ledger.adapter.out.persistence.repository;

import com.swiftpay.ledger.adapter.out.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {

    boolean existsByTransactionId(UUID transactionId);
}
