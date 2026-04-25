package com.paypal.transaction_service.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponse {

    private BigDecimal amount;

    private BigDecimal currentBalance;
}
