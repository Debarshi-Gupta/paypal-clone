package com.paypal.wallet_service.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalanceResponse {

    private Long userId;

    private BigDecimal balance;
}
