package com.paypal.notification_service.kafka.events;

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
public abstract class DepositResultEvent extends KafkaEvent {

    private Long depositId;
    private Long userId;
    private BigDecimal amount;
}
