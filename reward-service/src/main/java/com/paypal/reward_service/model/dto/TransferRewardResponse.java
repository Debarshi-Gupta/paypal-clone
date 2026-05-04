package com.paypal.reward_service.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRewardResponse {

    private Long id;

    private Long userId;

    private Long transferId;

    private BigDecimal transferAmount;

    private Integer rewardPoints;
}
