package com.paypal.reward_service.service;

import com.paypal.reward_service.kafka.events.TransferSucceededEvent;
import com.paypal.reward_service.model.dto.TransferRewardResponse;

import java.util.List;

public interface RewardService {

    List<TransferRewardResponse> getTransferRewardsByUserId(Long userId);

    TransferRewardResponse getTransferRewardByIdAndUserId(Long transferRewardId, Long userId);

    Integer getUserRewardPoints(Long userId);

    void processReward(TransferSucceededEvent event);
}
