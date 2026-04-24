package com.paypal.transaction_service.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionCreatedEvent extends KafkaEvent {

    private Long transactionId;

    private Long senderId;

    private Long receiverId;

    private BigDecimal amount;
}
