package com.paypal.transaction_service.service;

import com.paypal.transaction_service.exception.TransactionNotFoundException;
import com.paypal.transaction_service.exception.UnauthorizedTransactionException;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.kafka.events.KafkaEventType;
import com.paypal.transaction_service.kafka.events.TransactionCreatedEvent;
import com.paypal.transaction_service.model.dto.CreateTransactionRequest;
import com.paypal.transaction_service.model.dto.TransactionResponse;
import com.paypal.transaction_service.model.entity.Transaction;
import com.paypal.transaction_service.model.entity.TransactionStatus;
import com.paypal.transaction_service.repository.TransactionRepository;
import com.paypal.transaction_service.service.feign.UserClient;
import com.paypal.transaction_service.service.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final UserClient userClient;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${kafka.topic.transaction.created}")
    private String transactionCreatedTopic;

    @Override
    public TransactionResponse createTransaction(Long senderId, CreateTransactionRequest request) {

        log.info("Initiating transaction from {} to {}", senderId, request.getReceiverId());

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
                .status(TransactionStatus.PENDING)
                .build();

        Transaction savedTransaction = repository.save(txn);

        log.info("Transaction created with PENDING status. TransactionId={}", savedTransaction.getId());

        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .eventType(KafkaEventType.TRANSACTION_CREATED)
                .transactionId(savedTransaction.getId())
                .senderId(savedTransaction.getSenderId())
                .receiverId(savedTransaction.getReceiverId())
                .amount(savedTransaction.getAmount())
                .build();

        try {
            kafkaEventProducer.sendEvent(
                    transactionCreatedTopic,
                    String.valueOf(savedTransaction.getId()),
                    event
            );

            log.info("TransactionCreatedEvent published successfully for transactionId={}",
                    savedTransaction.getId());

        } catch (Exception ex) {
            log.error("Failed to publish transaction event for transactionId={}",
                    savedTransaction.getId(), ex);

            savedTransaction.setStatus(TransactionStatus.FAILED);
            repository.save(savedTransaction);

            throw new RuntimeException("Transaction event publishing failed");
        }

        return mapper.toResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getTransactionsBySender(Long senderId) {

        log.info("Fetching transactions for sender: {}", senderId);

        return repository.findBySenderId(senderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId, Long senderId) {

        log.info("Fetching transactionId={} for senderId={}", transactionId, senderId);

        Transaction transaction = repository.findByIdAndSenderId(transactionId, senderId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found or access denied. transactionId={}, senderId={}",
                            transactionId, senderId);
                    return new TransactionNotFoundException("Transaction not found");
                });

        log.info("Transaction found for transactionId={} and senderId={}", transactionId, senderId);

        return mapper.toResponse(transaction);
    }
}
