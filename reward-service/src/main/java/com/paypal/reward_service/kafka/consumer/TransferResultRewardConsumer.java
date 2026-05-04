package com.paypal.reward_service.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.reward_service.kafka.events.KafkaEvent;
import com.paypal.reward_service.kafka.events.TransferSucceededEvent;
import com.paypal.reward_service.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferResultRewardConsumer {

    private final RewardService rewardService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topic.transfer.reward}",
            groupId = "reward-group"
    )
    public void consume(String message) {

        log.info("Received transfer result reward event: {}", message);

        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event instanceof TransferSucceededEvent successEvent) {
                rewardService.processReward(successEvent);
            } else {
                log.info("Skipping reward for failed transfer event");
            }

        } catch (Exception ex) {
            log.error("Failed to process reward event", ex);
        }
    }
}
