package com.paypal.transaction_service.service;

import com.paypal.transaction_service.kafka.events.TransferResultEvent;
import com.paypal.transaction_service.model.dto.CreateTransferRequest;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.dto.TransferResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    BigDecimal getBalance(Long userId);

    DepositResponse deposit(Long userId, DepositRequest depositRequest);

    TransferResponse initiateTransfer(Long senderId, CreateTransferRequest request);

    List<TransferResponse> getTransfersBySenderId(Long senderId);

    TransferResponse getTransferById(Long transferId, Long senderId);

    TransferResultEvent handleTransferResult(String message);
}
