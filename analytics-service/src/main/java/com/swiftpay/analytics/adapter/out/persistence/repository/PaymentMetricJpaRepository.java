package com.swiftpay.analytics.adapter.out.persistence.repository;

import com.swiftpay.analytics.adapter.out.persistence.entity.PaymentMetricEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentMetricJpaRepository extends JpaRepository<PaymentMetricEntity, UUID> {

    boolean existsByTransactionId(UUID transactionId);

    List<PaymentMetricEntity> findAllByOrderByProcessedAtDesc(Pageable pageable);
}
