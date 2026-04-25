package com.paypal.transaction_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.kafka.events.KafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendEvent(String topic, String key, KafkaEvent event) {

        try {
            String message = objectMapper.writeValueAsString(event);

            log.info("Sending event -> Topic: {}, Key: {}, Payload: {}", topic, key, message);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, message);

            future.thenAccept(result -> {
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("Message sent successfully -> Topic: {}, Partition: {}, Offset: {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }).exceptionally(ex -> {
                log.error("Failed to send Kafka message: {}", ex.getMessage());
                return null;
            });

        } catch (Exception e) {
            log.error("Error serializing Kafka event", e);
            throw new RuntimeException("Kafka publish failed");
        }
    }
}
