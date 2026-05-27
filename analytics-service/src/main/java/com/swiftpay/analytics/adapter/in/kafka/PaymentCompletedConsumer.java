package com.swiftpay.analytics.adapter.in.kafka;

import com.swiftpay.analytics.application.AnalyticsApplicationService;
import com.swiftpay.shared.event.PaymentCompletedEvent;
import com.swiftpay.shared.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCompletedConsumer {

    private final AnalyticsApplicationService analyticsService;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_COMPLETED,
            groupId = "analytics-group",
            properties = "spring.json.value.default.type=com.swiftpay.shared.event.PaymentCompletedEvent"
    )
    public void consume(PaymentCompletedEvent event) {
        analyticsService.recordCompletedPayment(event);
    }
}
