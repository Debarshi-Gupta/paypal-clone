package com.paypal.reward_service.service;

import com.paypal.reward_service.kafka.events.TransferSucceededEvent;
import com.paypal.reward_service.model.dto.TransferRewardResponse;
import com.paypal.reward_service.model.dto.UserRewardPointsResponse;

import java.util.List;

public interface RewardService {

    List<TransferRewardResponse> getTransferRewardsByUserId(Long userId);

    TransferRewardResponse getTransferRewardByIdAndUserId(Long transferRewardId, Long userId);

    UserRewardPointsResponse getUserRewardPoints(Long userId);

    void processReward(TransferSucceededEvent event);
}
