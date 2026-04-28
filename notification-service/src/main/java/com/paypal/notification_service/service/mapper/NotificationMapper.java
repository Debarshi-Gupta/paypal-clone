package com.paypal.notification_service.service.mapper;

import com.paypal.notification_service.model.dto.NotificationResponse;
import com.paypal.notification_service.model.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .transactionId(notification.getTransactionId())
                .transactionType(notification.getTransactionType())
                .notificationType(notification.getNotificationType())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
