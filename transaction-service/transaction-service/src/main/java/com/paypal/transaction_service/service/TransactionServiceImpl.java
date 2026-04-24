package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.exception.TransferNotFoundException;
import com.paypal.transaction_service.exception.TransferProcessingException;
import com.paypal.transaction_service.exception.UnauthorizedTransferException;
import com.paypal.transaction_service.exception.UserNotFoundException;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.kafka.events.*;
import com.paypal.transaction_service.model.dto.CreateTransferRequest;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.dto.TransferResponse;
import com.paypal.transaction_service.model.entity.Transfer;
import com.paypal.transaction_service.model.entity.TransferStatus;
import com.paypal.transaction_service.repository.TransferRepository;
import com.paypal.transaction_service.service.feign.UserClient;
import com.paypal.transaction_service.service.feign.WalletClient;
import com.paypal.transaction_service.service.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransferRepository repository;
    private final TransferMapper mapper;
    private final UserClient userClient;
    private final WalletClient walletClient;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.transfer.initiated}")
    private String transferInitiatedTopic;

    @Override
    public BigDecimal getBalance(Long userId) {

        log.info("Checking balance of wallet for userId={}", userId);

        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        try {
            BigDecimal amount = walletClient.getBalance(userId);
            log.info("Balance amount {} fetched successfully for userId={}", amount, userId);
            return amount;
        } catch (Exception ex) {
            log.info("Failed to fetch balance amount of wallet for userId={}", userId);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public DepositResponse deposit(Long userId, DepositRequest depositRequest) {

        log.info("Depositing amount {} into wallet for userId={}", depositRequest.getAmount(), userId);

        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        try {
            DepositResponse depositResponse = walletClient.deposit(userId, depositRequest);
            log.info("Deposit of amount {} successful for wallet of userId={}", depositResponse.getAmount(), userId);
            return depositResponse;
        } catch (Exception ex) {
            log.info("Failed to deposit amount {} into wallet of userId={}", depositRequest.getAmount(), userId);
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public TransferResponse initiateTransfer(Long senderId, CreateTransferRequest request) {

        log.info("Initiating transfer from {} to {}", senderId, request.getReceiverId());

        if (senderId.equals(request.getReceiverId())) {
            throw new UnauthorizedTransferException("Sender and receiver cannot be same");
        }

        try {
            userClient.getUserById(senderId);
        } catch (Exception e) {
            log.error("Sender Id {} doesn't exist", senderId);
            throw new UserNotFoundException("Sender Id not found");
        }

        try {
            userClient.getUserById(request.getReceiverId());
        } catch (Exception e) {
            log.error("Receiver Id {} doesn't exist", request.getReceiverId());
            throw new UserNotFoundException("Receiver Id not found");
        }

        Transfer transfer = Transfer.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(TransferStatus.PENDING)
                .build();

        Transfer savedTransfer = repository.save(transfer);

        log.info("Transfer initiated with PENDING status. TransferId={}", savedTransfer.getId());

        TransferInitiatedEvent event = TransferInitiatedEvent.builder()
                .eventType(KafkaEventType.TRANSFER_INITIATED)
                .transferId(savedTransfer.getId())
                .senderId(savedTransfer.getSenderId())
                .receiverId(savedTransfer.getReceiverId())
                .amount(savedTransfer.getAmount())
                .build();

        try {
            kafkaEventProducer.sendEvent(
                    transferInitiatedTopic,
                    String.valueOf(savedTransfer.getId()),
                    event
            );

            log.info("TransferInitiatedEvent published successfully for transferId={}",
                    savedTransfer.getId());

        } catch (Exception ex) {
            log.error("Failed to publish transfer event for transferId={}",
                    savedTransfer.getId(), ex);

            savedTransfer.setStatus(TransferStatus.FAILED);
            repository.save(savedTransfer);

            throw new RuntimeException("Transfer event publishing failed");
        }

        return mapper.toResponse(savedTransfer);
    }

    @Override
    public List<TransferResponse> getTransfersBySenderId(Long senderId) {

        log.info("Fetching transfers for sender: {}", senderId);

        return repository.findBySenderId(senderId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TransferResponse getTransferById(Long transferId, Long senderId) {

        log.info("Fetching transferId={} for senderId={}", transferId, senderId);

        Transfer transfer = repository.findByIdAndSenderId(transferId, senderId)
                .orElseThrow(() -> {
                    log.warn("Transfer not found or access denied. transferId={}, senderId={}",
                            transferId, senderId);
                    return new TransferNotFoundException("Transfer not found");
                });

        log.info("Transfer found for transferId={} and senderId={}", transferId, senderId);

        return mapper.toResponse(transfer);
    }

    @Override
    public TransferResultEvent handleTransferResult(String message) {

        log.info("Processing transfer result event: {}", message);

        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event instanceof TransferSucceededEvent successEvent) {
                return handleTransferSucceededResult(successEvent);
            }

            if (event instanceof TransferFailedEvent failedEvent) {
                return handleTransferFailedResult(failedEvent);
            }

            throw new TransferProcessingException("Unsupported event type");

        } catch (Exception ex) {
            log.error("Failed to process transfer result event", ex);
            throw new TransferProcessingException(ex.getMessage());
        }
    }

    private TransferSucceededEvent handleTransferSucceededResult(TransferSucceededEvent event) {

        log.info("Handling SUCCESS event for transferId={}", event.getTransferId());

        Transfer txn = repository.findById(event.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));

        txn.setStatus(TransferStatus.SUCCESS);
        repository.save(txn);

        log.info("Transfer marked SUCCESS for id={}", txn.getId());

        return event;
    }

    private TransferFailedEvent handleTransferFailedResult(TransferFailedEvent event) {

        log.info("Handling FAILED event for transferId={}", event.getTransferId());

        Transfer txn = repository.findById(event.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));

        txn.setStatus(TransferStatus.FAILED);
        repository.save(txn);

        log.warn("Transfer marked FAILED for id={} reason={}",
                txn.getId(), event.getReason());

        return event;
    }
}
