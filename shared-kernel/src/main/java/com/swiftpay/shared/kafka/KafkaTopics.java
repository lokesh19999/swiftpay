package com.swiftpay.shared.kafka;

public final class KafkaTopics {

    public static final String PAYMENT_INITIATED = "payment.initiated";
    public static final String PAYMENT_COMPLETED = "payment.completed";
    public static final String PAYMENT_FAILED = "payment.failed";

    public static final String PAYMENT_INITIATED_DLT = "payment.initiated.DLT";
    public static final String PAYMENT_INITIATED_RETRY = "payment.initiated.retry";

    private KafkaTopics() {
    }
}
