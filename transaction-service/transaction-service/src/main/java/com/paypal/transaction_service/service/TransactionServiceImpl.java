package com.paypal.transaction_service.service;

import com.paypal.transaction_service.exception.UnauthorizedTransactionException;
import com.paypal.transaction_service.model.dto.CreateTransactionRequest;
import com.paypal.transaction_service.model.dto.TransactionResponse;
import com.paypal.transaction_service.model.entity.Transaction;
import com.paypal.transaction_service.repository.TransactionRepository;
import com.paypal.transaction_service.service.feign.UserClient;
import com.paypal.transaction_service.service.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final UserClient userClient;

    @Override
    public TransactionResponse createTransaction(Long senderId, CreateTransactionRequest request) {

        log.info("Creating transaction from sender: {} to receiver: {}", senderId, request.getReceiverId());

        if (senderId.equals(request.getReceiverId())) {
            throw new UnauthorizedTransactionException("Sender and receiver cannot be same");
        }

        userClient.getUserById(senderId);

        userClient.getUserById(request.getReceiverId());

        Transaction txn = Transaction.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        Transaction saved = repository.save(txn);

        log.info("Transaction created with id: {}", saved.getId());

        return mapper.toResponse(saved);
    }

    @Override
    public List<TransactionResponse> getTransactionsBySender(Long senderId) {

        log.info("Fetching transactions for sender: {}", senderId);

        return repository.findBySenderId(senderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
