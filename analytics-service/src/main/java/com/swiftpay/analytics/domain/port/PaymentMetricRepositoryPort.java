package com.swiftpay.analytics.domain.port;

import com.swiftpay.analytics.domain.model.PaymentMetric;

import java.util.List;
import java.util.UUID;

public interface PaymentMetricRepositoryPort {

    boolean existsByTransactionId(UUID transactionId);

    PaymentMetric save(PaymentMetric metric);

    List<PaymentMetric> findRecent(int limit);
}
