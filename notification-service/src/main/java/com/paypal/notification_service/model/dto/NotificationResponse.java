package com.paypal.notification_service.model.dto;

import com.paypal.notification_service.model.entity.NotificationType;
import com.paypal.notification_service.model.entity.TransactionType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private Long transactionId;

    private TransactionType transactionType;

    private NotificationType notificationType;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;
}
