package com.paypal.notification_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class TransferResultNotification {

    private Long transferId;

    private UserResponse sender;

    private UserResponse receiver;

    private BigDecimal amount;
}
