package com.paypal.transaction_service.kafka.events;

public enum KafkaEventType {

    TRANSFER_INITIATED,
    TRANSFER_SUCCEEDED,
    TRANSFER_FAILED,
    DEPOSIT_INITIATED,
    DEPOSIT_SUCCEEDED,
    DEPOSIT_FAILED
}
