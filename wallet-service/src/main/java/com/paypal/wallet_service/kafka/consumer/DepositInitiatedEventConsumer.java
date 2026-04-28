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
public class DepositInitiatedEventConsumer {

    private final WalletService walletService;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.deposit.result}")
    private String depositResultTopic;

    @KafkaListener(
            topics = "${kafka.topic.deposit.initiated}",
            groupId = "wallet-group"
    )
    public void consume(String message) {

        log.info("Received deposit initiated event: {}", message);

        try {
            DepositInitiatedEvent event =
                    objectMapper.readValue(message, DepositInitiatedEvent.class);

            processTransfer(event);

        } catch (Exception ex) {
            log.error("Failed to process Kafka message", ex);
        }
    }

    private void processTransfer(DepositInitiatedEvent event) {

        DepositSucceededEvent depositSucceededEvent;
        DepositFailedEvent depositFailedEvent;

        try {
            depositSucceededEvent = walletService.deposit(event);

            log.info("Wallet deposit successful for depositId={}", event.getDepositId());

            publishResult(depositSucceededEvent);

        } catch (Exception ex) {

            log.error("Wallet deposit failed for depositId={}", event.getDepositId(), ex);

            depositFailedEvent = DepositFailedEvent.builder()
                    .depositId(event.getDepositId())
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .reason(ex.getMessage())
                    .build();

            publishResult(depositFailedEvent);
        }
    }

    private void publishResult(DepositResultEvent event) {

        try {
            kafkaEventProducer.sendEvent(
                    depositResultTopic,
                    String.valueOf(event.getDepositId()),
                    event
            );

            log.info("DepositResultEvent published for depositId={}",
                    event.getDepositId());

        } catch (Exception ex) {
            log.error("Failed to publish result event for depositId={}",
                    event.getDepositId(), ex);
        }
    }
}
