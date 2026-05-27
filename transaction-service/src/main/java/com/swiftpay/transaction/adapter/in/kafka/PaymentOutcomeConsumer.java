package com.swiftpay.transaction.adapter.in.kafka;

import com.swiftpay.shared.event.PaymentCompletedEvent;
import com.swiftpay.shared.event.PaymentFailedEvent;
import com.swiftpay.shared.kafka.KafkaTopics;
import com.swiftpay.transaction.application.PaymentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentOutcomeConsumer {

    private final PaymentApplicationService paymentService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "transaction-outcome-group",
            properties = "spring.json.value.default.type=com.swiftpay.shared.event.PaymentCompletedEvent"
    )
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        log.info("payment_completed_received transactionId={}", event.transactionId());
        paymentService.markCompleted(event.transactionId());
    }

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_FAILED,
            groupId = "transaction-outcome-group",
            properties = "spring.json.value.default.type=com.swiftpay.shared.event.PaymentFailedEvent"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("payment_failed_received transactionId={} reason={}", event.transactionId(), event.failureReason());
        paymentService.markFailed(event.transactionId(), event.failureReason());
    }
}
