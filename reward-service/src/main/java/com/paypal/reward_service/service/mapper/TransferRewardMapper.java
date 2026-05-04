package com.paypal.reward_service.service.mapper;

import com.paypal.reward_service.model.dto.TransferRewardResponse;
import com.paypal.reward_service.model.entity.TransferReward;
import org.springframework.stereotype.Component;

@Component
public class TransferRewardMapper {

    public TransferRewardResponse toResponse(TransferReward transferReward) {
        return TransferRewardResponse.builder()
                .id(transferReward.getId())
                .userId(transferReward.getUserId())
                .transferId(transferReward.getTransferId())
                .transferAmount(transferReward.getTransferAmount())
                .rewardPoints(transferReward.getRewardPoints())
                .build();
    }
}
