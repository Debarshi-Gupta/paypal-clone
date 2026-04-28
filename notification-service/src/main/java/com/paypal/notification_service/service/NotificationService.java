package com.paypal.notification_service.service;

import com.paypal.notification_service.model.dto.DepositResultNotification;
import com.paypal.notification_service.model.dto.NotificationResponse;
import com.paypal.notification_service.model.dto.TransferResultNotification;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotificationsByUserId(Long userId);

    NotificationResponse getNotificationByIdAndUserId(Long notificationId, Long userId);

    NotificationResponse markNotificationAsRead(Long notificationId, Long userId);

    DepositResultNotification handleDepositResultNotification(String message);

    TransferResultNotification handleTransferResultNotification(String message);
}
