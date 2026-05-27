package com.swiftpay.ledger.domain.port;

import com.swiftpay.shared.event.PaymentInitiatedEvent;

public interface PaymentOutcomePublisherPort {

    void publishCompleted(PaymentInitiatedEvent event);

    void publishFailed(PaymentInitiatedEvent event, String reason);
}
