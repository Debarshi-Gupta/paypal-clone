package com.paypal.transaction_service.kafka.consumer;

import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.kafka.events.KafkaEvent;
import com.paypal.transaction_service.kafka.events.TransferFailedEvent;
import com.paypal.transaction_service.kafka.events.TransferResultEvent;
import com.paypal.transaction_service.kafka.events.TransferSucceededEvent;
import com.paypal.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultConsumer {

    private final TransactionService transactionService;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${kafka.topic.transfer.notification}")
    private String transferNotificationTopic;

    @KafkaListener(
            topics = "${kafka.topic.transfer.result}",
            groupId = "transfer-group"
    )
    public void consume(String message) {

        log.info("Received transfer result event: {}", message);

        try {
            TransferResultEvent transferResultEvent = transactionService.handleTransferResult(message);
            publishNotification(transferResultEvent);
        } catch (Exception ex) {
            log.error("Error processing transfer result event", ex);
        }
    }

    private void publishNotification(KafkaEvent event) {

        try {
            kafkaEventProducer.sendEvent(
                    transferNotificationTopic,
                    null,
                    event
            );

            log.info("Notification event published for transferId={}",
                    extractTransferId(event));

        } catch (Exception ex) {
            log.error("Failed to publish notification event", ex);
        }
    }

    private Long extractTransferId(KafkaEvent event) {
        if (event instanceof TransferSucceededEvent e) return e.getTransferId();
        if (event instanceof TransferFailedEvent e) return e.getTransferId();
        return null;
    }
}
