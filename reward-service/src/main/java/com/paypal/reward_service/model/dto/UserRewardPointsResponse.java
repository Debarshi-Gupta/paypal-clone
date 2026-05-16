package com.paypal.reward_service.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRewardPointsResponse {

    private Long userId;

    private Integer userRewardPoints;
}
