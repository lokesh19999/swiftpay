package com.swiftpay.ledger.adapter.in.kafka;

import com.swiftpay.ledger.application.LedgerApplicationService;
import com.swiftpay.shared.event.PaymentInitiatedEvent;
import com.swiftpay.shared.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentInitiatedConsumer {

    private final LedgerApplicationService ledgerService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_INITIATED,
            groupId = "ledger-group",
            containerFactory = "paymentInitiatedKafkaListenerContainerFactory"
    )
    public void consume(PaymentInitiatedEvent event) {
        log.info("payment_initiated_received transactionId={}", event.transactionId());
        ledgerService.processPayment(event);
    }
}
