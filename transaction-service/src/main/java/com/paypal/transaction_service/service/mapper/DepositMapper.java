package com.paypal.transaction_service.service.mapper;

import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.entity.Deposit;
import org.springframework.stereotype.Component;

@Component
public class DepositMapper {

    public DepositResponse toResponse(Deposit deposit) {
        return DepositResponse.builder()
                .depositId(deposit.getId())
                .userId(deposit.getUserId())
                .amount(deposit.getAmount())
                .status(deposit.getStatus().name())
                .build();
    }
}
