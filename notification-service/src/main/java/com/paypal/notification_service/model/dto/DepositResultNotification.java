package com.paypal.notification_service.model.dto;

import com.paypal.notification_service.kafka.events.KafkaEvent;
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
public abstract class DepositResultNotification extends KafkaEvent {

    private Long depositId;

    private UserResponse user;

    private BigDecimal amount;
}
