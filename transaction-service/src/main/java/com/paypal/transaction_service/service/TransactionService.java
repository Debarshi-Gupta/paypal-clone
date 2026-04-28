package com.paypal.transaction_service.service;

import com.paypal.transaction_service.kafka.events.DepositResultEvent;
import com.paypal.transaction_service.kafka.events.TransferResultEvent;
import com.paypal.transaction_service.model.dto.CreateTransferRequest;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.dto.TransferResponse;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    BigDecimal getBalance(Long userId);

    List<DepositResponse> getDepositsByUserId(Long userId);

    DepositResponse getDepositByIdAndUserId(Long depositId, Long userId);

    DepositResponse initiateDeposit(Long userId, DepositRequest depositRequest);

    DepositResultEvent handleDepositResult(String message);

    List<TransferResponse> getTransfersBySenderId(Long senderId);

    TransferResponse getTransferByIdAndSenderId(Long transferId, Long senderId);

    TransferResponse initiateTransfer(Long senderId, CreateTransferRequest request);

    TransferResultEvent handleTransferResult(String message);


}
