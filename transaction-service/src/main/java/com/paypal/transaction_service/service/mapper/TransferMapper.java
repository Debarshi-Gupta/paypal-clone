package com.paypal.transaction_service.service.mapper;

import com.paypal.transaction_service.model.dto.TransferResponse;
import com.paypal.transaction_service.model.entity.Transfer;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferResponse toResponse(Transfer transfer) {
        return TransferResponse.builder()
                .transfer_id(transfer.getId())
                .senderId(transfer.getSenderId())
                .receiverId(transfer.getReceiverId())
                .amount(transfer.getAmount())
                .status(transfer.getStatus().name())
                .description(transfer.getDescription())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}