package com.swiftpay.transaction.adapter.out.kafka;

import com.swiftpay.shared.event.PaymentInitiatedEvent;
import com.swiftpay.shared.kafka.KafkaTopics;
import com.swiftpay.transaction.domain.model.Transaction;
import com.swiftpay.transaction.domain.port.PaymentEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentEventPublisher implements PaymentEventPublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishPaymentInitiated(Transaction transaction) {
        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                transaction.id(),
                transaction.idempotencyKey(),
                transaction.senderAccountId(),
                transaction.receiverAccountId(),
                transaction.amount(),
                transaction.currency(),
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_INITIATED, transaction.id().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("kafka_publish_failed topic={} transactionId={}", KafkaTopics.PAYMENT_INITIATED, transaction.id(), ex);
                    }
                });
    }
}
