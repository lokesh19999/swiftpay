package com.swiftpay.ledger.adapter.out.kafka;

import com.swiftpay.shared.event.PaymentCompletedEvent;
import com.swiftpay.shared.event.PaymentFailedEvent;
import com.swiftpay.shared.event.PaymentInitiatedEvent;
import com.swiftpay.shared.kafka.KafkaTopics;
import com.swiftpay.ledger.domain.port.PaymentOutcomePublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class KafkaPaymentOutcomePublisher implements PaymentOutcomePublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishCompleted(PaymentInitiatedEvent event) {
        PaymentCompletedEvent completed = new PaymentCompletedEvent(
                event.transactionId(),
                event.senderAccountId(),
                event.receiverAccountId(),
                event.amount(),
                event.currency(),
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_COMPLETED, event.transactionId().toString(), completed);
    }

    @Override
    public void publishFailed(PaymentInitiatedEvent event, String reason) {
        PaymentFailedEvent failed = new PaymentFailedEvent(
                event.transactionId(),
                event.senderAccountId(),
                event.receiverAccountId(),
                event.amount(),
                event.currency(),
                reason,
                Instant.now()
        );
        kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, event.transactionId().toString(), failed);
    }
}
