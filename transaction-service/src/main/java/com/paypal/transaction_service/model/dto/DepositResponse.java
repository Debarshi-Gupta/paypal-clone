package com.paypal.transaction_service.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositResponse {

    private Long depositId;

    private Long userId;

    private BigDecimal amount;

    private String status;
}
