package com.paypal.transaction_service.service;

import com.paypal.transaction_service.model.dto.CreateTransactionRequest;
import com.paypal.transaction_service.model.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(Long senderId, CreateTransactionRequest request);

    List<TransactionResponse> getTransactionsBySender(Long senderId);
}
