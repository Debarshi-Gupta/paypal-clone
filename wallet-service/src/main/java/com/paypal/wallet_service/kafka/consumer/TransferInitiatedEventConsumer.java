package com.paypal.wallet_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.wallet_service.kafka.KafkaEventProducer;
import com.paypal.wallet_service.kafka.events.*;
import com.paypal.wallet_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferInitiatedEventConsumer {

    private final WalletService walletService;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.transfer.result}")
    private String transferResultTopic;

    @KafkaListener(
            topics = "${kafka.topic.transfer.initiated}",
            groupId = "wallet-group"
    )
    public void consume(String message) {

        log.info("Received transfer initiated event: {}", message);

        try {
            TransferInitiatedEvent event =
                    objectMapper.readValue(message, TransferInitiatedEvent.class);

            processTransfer(event);

        } catch (Exception ex) {
            log.error("Failed to process Kafka message", ex);
        }
    }

    private void processTransfer(TransferInitiatedEvent event) {

        TransferSucceededEvent transferSucceededEvent;
        TransferFailedEvent transferFailedEvent;

        try {
            transferSucceededEvent = walletService.transfer(event);

            log.info("Wallet transfer successful for transferId={}", event.getTransferId());

            publishResult(transferSucceededEvent);

        } catch (Exception ex) {

            log.error("Wallet transfer failed for transferId={}", event.getTransferId(), ex);

            transferFailedEvent = TransferFailedEvent.builder()
                    .eventType(KafkaEventType.TRANSFER_FAILED)
                    .transferId(event.getTransferId())
                    .senderId(event.getSenderId())
                    .receiverId(event.getReceiverId())
                    .reason(ex.getMessage())
                    .build();

            publishResult(transferFailedEvent);
        }
    }

    private void publishResult(TransferResultEvent event) {

        try {
            kafkaEventProducer.sendEvent(
                    transferResultTopic,
                    String.valueOf(event.getTransferId()),
                    event
            );

            log.info("TransferResultEvent published for transferId={}",
                    event.getTransferId());

        } catch (Exception ex) {
            log.error("Failed to publish result event for transferId={}",
                    event.getTransferId(), ex);
        }
    }
}
