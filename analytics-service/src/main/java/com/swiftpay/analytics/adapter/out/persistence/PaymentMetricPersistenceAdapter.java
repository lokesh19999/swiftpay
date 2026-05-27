package com.swiftpay.analytics.adapter.out.persistence;

import com.swiftpay.analytics.adapter.out.persistence.entity.PaymentMetricEntity;
import com.swiftpay.analytics.adapter.out.persistence.repository.PaymentMetricJpaRepository;
import com.swiftpay.analytics.domain.model.PaymentMetric;
import com.swiftpay.analytics.domain.port.PaymentMetricRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentMetricPersistenceAdapter implements PaymentMetricRepositoryPort {

    private final PaymentMetricJpaRepository repository;

    @Override
    public boolean existsByTransactionId(UUID transactionId) {
        return repository.existsByTransactionId(transactionId);
    }

    @Override
    public PaymentMetric save(PaymentMetric metric) {
        PaymentMetricEntity entity = new PaymentMetricEntity();
        entity.setId(metric.id());
        entity.setTransactionId(metric.transactionId());
        entity.setSenderAccountId(metric.senderAccountId());
        entity.setReceiverAccountId(metric.receiverAccountId());
        entity.setAmount(metric.amount());
        entity.setCurrency(metric.currency());
        entity.setProcessedAt(metric.processedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<PaymentMetric> findRecent(int limit) {
        return repository.findAllByOrderByProcessedAtDesc(PageRequest.of(0, limit))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private PaymentMetric toDomain(PaymentMetricEntity entity) {
        return new PaymentMetric(
                entity.getId(),
                entity.getTransactionId(),
                entity.getSenderAccountId(),
                entity.getReceiverAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getProcessedAt()
        );
    }
}
