package com.paypal.transaction_service.service.mapper;

import com.paypal.transaction_service.model.dto.TransactionResponse;
import com.paypal.transaction_service.model.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .senderId(txn.getSenderId())
                .receiverId(txn.getReceiverId())
                .amount(txn.getAmount())
                .status(txn.getStatus().name())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}