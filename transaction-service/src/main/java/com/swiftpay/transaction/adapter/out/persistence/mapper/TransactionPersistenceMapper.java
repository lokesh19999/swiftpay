package com.swiftpay.transaction.adapter.out.persistence.mapper;

import com.swiftpay.transaction.adapter.out.persistence.entity.TransactionEntity;
import com.swiftpay.transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public TransactionEntity toEntity(Transaction domain) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.id());
        entity.setIdempotencyKey(domain.idempotencyKey());
        entity.setSenderAccountId(domain.senderAccountId());
        entity.setReceiverAccountId(domain.receiverAccountId());
        entity.setAmount(domain.amount());
        entity.setCurrency(domain.currency());
        entity.setStatus(domain.status());
        entity.setFailureReason(domain.failureReason());
        entity.setCreatedAt(domain.createdAt());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }

    public Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getIdempotencyKey(),
                entity.getSenderAccountId(),
                entity.getReceiverAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
