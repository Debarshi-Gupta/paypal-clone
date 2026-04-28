package com.paypal.notification_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.notification_service.exception.DepositNotificationProcessingException;
import com.paypal.notification_service.exception.NotificationNotFoundException;
import com.paypal.notification_service.exception.TransferNotificationProcessingException;
import com.paypal.notification_service.exception.UserNotFoundException;
import com.paypal.notification_service.kafka.events.*;
import com.paypal.notification_service.model.dto.*;
import com.paypal.notification_service.model.entity.Notification;
import com.paypal.notification_service.model.entity.NotificationType;
import com.paypal.notification_service.model.entity.TransactionType;
import com.paypal.notification_service.repository.NotificationRepository;
import com.paypal.notification_service.service.feign.UserClient;
import com.paypal.notification_service.service.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserClient userClient;
    private final ObjectMapper objectMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationResponse> getNotificationsByUserId(Long userId) {

        log.info("Fetching all notifications for userId={}", userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        List<Notification> notifications = notificationRepository.findByUserId(user.getId());

        return notifications.stream().map(notificationMapper::toResponse).toList();
    }

    @Override
    public NotificationResponse getNotificationByIdAndUserId(Long notificationId, Long userId) {
        log.info("Fetching notificationId {} for userId={}", notificationId, userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Notification not found or access denied. notificationId={}, userId={}",
                            notificationId, user.getId());
                    return new NotificationNotFoundException("Notification not found with notificationId " + notificationId + " for userId=" + user.getId());
                });

        log.info("Notification found with notificationId " + notificationId + " for userId=" + user.getId());

        return notificationMapper.toResponse(notification);
    }

    @Override
    public NotificationResponse markNotificationAsRead(Long notificationId, Long userId) {
        log.info("Fetching notificationId {} for userId={}", notificationId, userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Notification not found or access denied. notificationId={}, userId={}",
                            notificationId, user.getId());
                    return new NotificationNotFoundException("Notification not found with notificationId " + notificationId + " for userId=" + user.getId());
                });

        log.info("Notification fetched with notificationId " + notificationId + " for userId=" + user.getId());

        notification.setRead(true);

        return notificationMapper.toResponse(notification);
    }

    @Override
    public DepositResultNotification handleDepositResultNotification(String message) {

        log.info("Processing deposit result notification event: {}", message);

        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event instanceof DepositSucceededEvent succeededEvent) {
                return handleDepositSucceededNotification(succeededEvent);
            }

            if (event instanceof DepositFailedEvent failedEvent) {
                return handleDepositFailedNotification(failedEvent);
            }

            throw new DepositNotificationProcessingException("Unsupported event type");

        } catch (Exception ex) {
            log.error("Failed to process deposit result event notification", ex);
            throw new DepositNotificationProcessingException(ex.getMessage());
        }
    }

    private DepositSucceededNotification handleDepositSucceededNotification(DepositSucceededEvent event) {

        log.info("Processing SUCCESS notification for depsoitId={}", event.getDepositId());

        UserResponse user;

        try {
            user = userClient.getUserById(event.getUserId());
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", event.getUserId());
            throw new UserNotFoundException("User Id not found");
        }

        Notification userNotification = Notification.builder()
                .userId(user.getId())
                .transactionId(event.getDepositId())
                .transactionType(TransactionType.DEPOSIT)
                .notificationType(NotificationType.DEPOSIT_SUCCEEDED)
                .message("You deposited " + event.getAmount() + " your wallet ")
                .build();
        notificationRepository.save(userNotification);
        log.info("Notification saved for userId={}", user.getId());

        return DepositSucceededNotification.builder()
                .depositId(event.getDepositId())
                .user(user)
                .amount(event.getAmount())
                .userBalance(event.getUserBalance())
                .build();
    }

    private DepositFailedNotification handleDepositFailedNotification(DepositFailedEvent event) {

        log.info("Processing FAILED notification for depositId={}", event.getDepositId());

        UserResponse user;

        try {
            user = userClient.getUserById(event.getUserId());
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", event.getUserId());
            throw new UserNotFoundException("User Id not found");
        }

        Notification userNotification = Notification.builder()
                .userId(user.getId())
                .transactionId(event.getDepositId())
                .transactionType(TransactionType.DEPOSIT)
                .notificationType(NotificationType.DEPOSIT_FAILED)
                .message("Your deposit failed. Reason: " + event.getReason())
                .build();
        notificationRepository.save(userNotification);
        log.info("Notification saved for userId={}", user.getId());

        return DepositFailedNotification.builder()
                .depositId(event.getDepositId())
                .user(user)
                .amount(event.getAmount())
                .reason(event.getReason())
                .build();
    }

    @Override
    public TransferResultNotification handleTransferResultNotification(String message) {

        log.info("Processing transfer result notification event: {}", message);

        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event instanceof TransferSucceededEvent successEvent) {
                return handleTransferSucceededNotification(successEvent);
            }

            if (event instanceof TransferFailedEvent failedEvent) {
                return handleTransferFailedNotification(failedEvent);
            }

            throw new TransferNotificationProcessingException("Unsupported event type");

        } catch (Exception ex) {
            log.error("Failed to process transfer result event notification", ex);
            throw new TransferNotificationProcessingException(ex.getMessage());
        }
    }

    private TransferSucceededNotification handleTransferSucceededNotification(TransferSucceededEvent event) {

        log.info("Processing SUCCESS notification for transferId={}", event.getTransferId());

        UserResponse sender, receiver;

        try {
            sender = userClient.getUserById(event.getSenderId());
        } catch (Exception e) {
            log.error("Sender Id {} doesn't exist", event.getSenderId());
            throw new UserNotFoundException("Sender Id not found");
        }

        try {
            receiver = userClient.getUserById(event.getReceiverId());
        } catch (Exception e) {
            log.error("Receiver Id {} doesn't exist", event.getReceiverId());
            throw new UserNotFoundException("Receiver Id not found");
        }

        Notification senderNotification = Notification.builder()
                .userId(sender.getId())
                .transactionId(event.getTransferId())
                .transactionType(TransactionType.TRANSFER)
                .notificationType(NotificationType.TRANSFER_SUCCEEDED)
                .message("You sent " + event.getAmount() + " to userId " + event.getReceiverId())
                .build();
        notificationRepository.save(senderNotification);
        log.info("Notification saved for senderId={}", sender.getId());

        Notification receiverNotification = Notification.builder()
                .userId(receiver.getId())
                .transactionId(event.getTransferId())
                .transactionType(TransactionType.TRANSFER)
                .notificationType(NotificationType.TRANSFER_SUCCEEDED)
                .message("You received " + event.getAmount() + " from userId " + event.getSenderId())
                .build();
        notificationRepository.save(receiverNotification);
        log.info("Notification saved for receiverId={}", receiver.getId());

        return TransferSucceededNotification.builder()
                .transferId(event.getTransferId())
                .sender(sender)
                .receiver(receiver)
                .amount(event.getAmount())
                .senderBalance(event.getSenderBalance())
                .receiverBalance(event.getReceiverBalance())
                .build();
    }

    private TransferFailedNotification handleTransferFailedNotification(TransferFailedEvent event) {

        log.info("Processing FAILED notification for transferId={}", event.getTransferId());

        UserResponse sender, receiver;

        try {
            sender = userClient.getUserById(event.getSenderId());
        } catch (Exception e) {
            log.error("Sender Id {} doesn't exist", event.getSenderId());
            throw new UserNotFoundException("Sender Id not found");
        }

        try {
            receiver = userClient.getUserById(event.getReceiverId());
        } catch (Exception e) {
            log.error("Receiver Id {} doesn't exist", event.getReceiverId());
            throw new UserNotFoundException("Receiver Id not found");
        }

        Notification senderNotification = Notification.builder()
                .userId(sender.getId())
                .transactionId(event.getTransferId())
                .transactionType(TransactionType.TRANSFER)
                .notificationType(NotificationType.TRANSFER_SUCCEEDED)
                .message("Your transfer failed. Reason: " + event.getReason())
                .build();
        notificationRepository.save(senderNotification);
        log.info("Notification saved for senderId={}", sender.getId());

        return TransferFailedNotification.builder()
                .transferId(event.getTransferId())
                .sender(sender)
                .receiver(receiver)
                .amount(event.getAmount())
                .reason(event.getReason())
                .build();
    }
}
