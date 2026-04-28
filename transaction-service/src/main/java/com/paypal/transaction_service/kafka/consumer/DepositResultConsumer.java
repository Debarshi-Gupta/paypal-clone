package com.paypal.transaction_service.kafka.consumer;

import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.kafka.events.DepositFailedEvent;
import com.paypal.transaction_service.kafka.events.DepositResultEvent;
import com.paypal.transaction_service.kafka.events.DepositSucceededEvent;
import com.paypal.transaction_service.kafka.events.KafkaEvent;
import com.paypal.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositResultConsumer {

    private final TransactionService transactionService;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${kafka.topic.deposit.notification}")
    private String depositNotificationTopic;

    @KafkaListener(
            topics = "${kafka.topic.deposit.result}",
            groupId = "deposit-group"
    )
    public void consume(String message) {

        log.info("Received deposit result event: {}", message);

        try {
            DepositResultEvent event = transactionService.handleDepositResult(message);
            publishNotification(event);

        } catch (Exception ex) {
            log.error("Error processing deposit result", ex);
        }
    }

    private void publishNotification(KafkaEvent event) {

        try {
            kafkaEventProducer.sendEvent(
                    depositNotificationTopic,
                    null,
                    event
            );

            log.info("Deposit notification sent for depositId={}",
                    extractDepositId(event));

        } catch (Exception ex) {
            log.error("Failed to publish deposit notification", ex);
        }
    }

    private Long extractDepositId(KafkaEvent event) {
        if (event instanceof DepositSucceededEvent e) return e.getDepositId();
        if (event instanceof DepositFailedEvent e) return e.getDepositId();
        return null;
    }
}
