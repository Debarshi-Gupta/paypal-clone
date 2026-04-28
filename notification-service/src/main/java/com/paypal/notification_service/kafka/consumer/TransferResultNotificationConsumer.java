package com.paypal.notification_service.kafka.consumer;

import com.paypal.notification_service.exception.TransferNotificationProcessingException;
import com.paypal.notification_service.model.dto.TransferFailedNotification;
import com.paypal.notification_service.model.dto.TransferResultNotification;
import com.paypal.notification_service.model.dto.TransferSucceededNotification;
import com.paypal.notification_service.service.EmailService;
import com.paypal.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultNotificationConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(
            topics = "${kafka.topic.transfer.notification}",
            groupId = "notification-group"
    )
    public void consume(String message) {

        log.info("Received transfer result event: {}", message);

        try {
            TransferResultNotification transferResultNotification = notificationService.handleTransferResultNotification(message);

            if (transferResultNotification instanceof TransferSucceededNotification transferSucceededNotification) {
                emailService.sendEmail(
                        transferSucceededNotification.getSender().getEmail(),
                        "Transfer Successful",
                        "Hi " + transferSucceededNotification.getSender().getName() +
                                ", your transfer of ₹" + transferSucceededNotification.getAmount() +
                                " to " + transferSucceededNotification.getReceiver().getName() + " was successful." +
                                "Your current balance is ₹" + transferSucceededNotification.getSenderBalance()
                );
                emailService.sendEmail(
                        transferSucceededNotification.getReceiver().getEmail(),
                        "Money Received",
                        "Hi " + transferSucceededNotification.getReceiver().getName() +
                                ", you have received ₹" + transferSucceededNotification.getAmount() +
                                " from " + transferSucceededNotification.getSender().getName() + "." +
                                "Your current balance is ₹" + transferSucceededNotification.getReceiverBalance()
                );
            }
            else if (transferResultNotification instanceof TransferFailedNotification transferFailedNotification) {
                emailService.sendEmail(
                        transferFailedNotification.getSender().getEmail(),
                        "Transfer Failed",
                        "Hi " + transferFailedNotification.getSender().getName() +
                                ", your transfer of ₹" + transferFailedNotification.getAmount() +
                                " failed. Reason: " + transferFailedNotification.getReason()
                );
            }
            else {
                throw new TransferNotificationProcessingException("Unsupported Transfer Result Type");
            }
        } catch (Exception ex) {
            log.error("Error processing transfer result event", ex);
            throw new TransferNotificationProcessingException(ex.getMessage());
        }
    }
}