package com.paypal.notification_service.kafka.consumer;

import com.paypal.notification_service.exception.DepositNotificationProcessingException;
import com.paypal.notification_service.model.dto.DepositFailedNotification;
import com.paypal.notification_service.model.dto.DepositResultNotification;
import com.paypal.notification_service.model.dto.DepositSucceededNotification;
import com.paypal.notification_service.service.EmailService;
import com.paypal.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositResultNotificationConsumer {

    private final NotificationService notificationService;
    private final EmailService emailService;

    @KafkaListener(
            topics = "${kafka.topic.deposit.notification}",
            groupId = "notification-group"
    )
    public void consume(String message) {

        log.info("Received deposit result event: {}", message);

        try {
            DepositResultNotification depositResultNotification = notificationService.handleDepositResultNotification(message);

            if (depositResultNotification instanceof DepositSucceededNotification depositSucceededNotification) {
                emailService.sendEmail(
                        depositSucceededNotification.getUser().getEmail(),
                        "Deposit Successful",
                        "Hi " + depositSucceededNotification.getUser().getName() +
                                ", your deposit of ₹" + depositSucceededNotification.getAmount() +
                                " was successful. Your current balance is ₹" + depositSucceededNotification.getUserBalance()
                );
            }
            else if (depositResultNotification instanceof DepositFailedNotification depositFailedNotification) {
                emailService.sendEmail(
                        depositFailedNotification.getUser().getEmail(),
                        "Deposit Failed",
                        "Hi " + depositFailedNotification.getUser().getName() +
                                ", your deposit of ₹" + depositFailedNotification.getAmount() +
                                " failed. Reason: " + depositFailedNotification.getReason()
                );
            }
            else {
                throw new DepositNotificationProcessingException("Unsupported Deposit Result Type");
            }
        } catch (Exception ex) {
            log.error("Error processing deposit result event", ex);
            throw new DepositNotificationProcessingException(ex.getMessage());
        }
    }
}
