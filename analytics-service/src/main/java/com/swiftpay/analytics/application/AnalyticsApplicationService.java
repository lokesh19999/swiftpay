package com.swiftpay.analytics.application;

import com.swiftpay.analytics.domain.model.PaymentMetric;
import com.swiftpay.analytics.domain.port.PaymentMetricRepositoryPort;
import com.swiftpay.shared.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsApplicationService {

    private final PaymentMetricRepositoryPort metricRepository;

    @Transactional
    public void recordCompletedPayment(PaymentCompletedEvent event) {
        if (metricRepository.existsByTransactionId(event.transactionId())) {
            return;
        }
        PaymentMetric metric = new PaymentMetric(
                UUID.randomUUID(),
                event.transactionId(),
                event.senderAccountId(),
                event.receiverAccountId(),
                event.amount(),
                event.currency(),
                event.occurredAt()
        );
        metricRepository.save(metric);
        log.info("payment_metric_recorded transactionId={} amount={}", event.transactionId(), event.amount());
    }
}
