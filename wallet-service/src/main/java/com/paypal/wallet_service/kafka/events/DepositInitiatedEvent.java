package com.paypal.wallet_service.kafka.events;

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
public class DepositInitiatedEvent extends KafkaEvent {

    private Long depositId;
    private Long userId;
    private BigDecimal amount;
}
