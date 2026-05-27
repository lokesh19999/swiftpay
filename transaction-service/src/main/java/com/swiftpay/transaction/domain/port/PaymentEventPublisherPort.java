package com.swiftpay.transaction.domain.port;

import com.swiftpay.transaction.domain.model.Transaction;

public interface PaymentEventPublisherPort {

    void publishPaymentInitiated(Transaction transaction);
}
