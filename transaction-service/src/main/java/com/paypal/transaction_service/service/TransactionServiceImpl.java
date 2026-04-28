package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.exception.*;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.kafka.events.*;
import com.paypal.transaction_service.model.dto.CreateTransferRequest;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.dto.TransferResponse;
import com.paypal.transaction_service.model.entity.Deposit;
import com.paypal.transaction_service.model.entity.DepositStatus;
import com.paypal.transaction_service.model.entity.Transfer;
import com.paypal.transaction_service.model.entity.TransferStatus;
import com.paypal.transaction_service.repository.DepositRepository;
import com.paypal.transaction_service.repository.TransferRepository;
import com.paypal.transaction_service.service.feign.UserClient;
import com.paypal.transaction_service.service.feign.WalletClient;
import com.paypal.transaction_service.service.mapper.DepositMapper;
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

    private final TransferRepository transferRepository;
    private final DepositRepository depositRepository;
    private final TransferMapper transferMapper;
    private final DepositMapper depositMapper;
    private final UserClient userClient;
    private final WalletClient walletClient;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.transfer.initiated}")
    private String transferInitiatedTopic;

    @Value("${kafka.topic.deposit.initiated}")
    private String depositInitiatedTopic;

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
    public List<DepositResponse> getDepositsByUserId(Long userId) {

        log.info("Fetching deposits for user: {}", userId);

        return depositRepository.findByUserId(userId)
                .stream()
                .map(depositMapper::toResponse)
                .toList();
    }

    @Override
    public DepositResponse getDepositByIdAndUserId(Long depositId, Long userId) {

        log.info("Fetching depositId={} for userId={}", depositId, userId);

        Deposit deposit = depositRepository.findByIdAndUserId(depositId, userId)
                .orElseThrow(() -> {
                    log.warn("Deposit not found or access denied. depositId={}, userId={}",
                            depositId, userId);
                    return new TransferNotFoundException("Transfer not found");
                });

        log.info("Deposit found for depositId={} and userId={}", depositId, userId);

        return depositMapper.toResponse(deposit);
    }

    @Override
    public DepositResponse initiateDeposit(Long userId, DepositRequest request) {

        log.info("Initiating deposit for userId={} amount={}", userId, request.getAmount());

        userClient.getUserById(userId);

        Deposit deposit = Deposit.builder()
                .userId(userId)
                .amount(request.getAmount())
                .status(DepositStatus.PENDING)
                .build();

        Deposit saved = depositRepository.save(deposit);

        log.info("Deposit created with PENDING status. depositId={}", saved.getId());

        DepositInitiatedEvent event = DepositInitiatedEvent.builder()
                .eventType(KafkaEventType.DEPOSIT_INITIATED)
                .depositId(saved.getId())
                .userId(saved.getUserId())
                .amount(saved.getAmount())
                .build();

        try {
            kafkaEventProducer.sendEvent(
                    depositInitiatedTopic,
                    String.valueOf(saved.getId()),
                    event
            );

            log.info("DepositInitiatedEvent published for depositId={}", saved.getId());

        } catch (Exception ex) {
            log.error("Failed to publish deposit event", ex);

            saved.setStatus(DepositStatus.FAILED);
            depositRepository.save(saved);

            throw new DepositProcessingException("Deposit initiation failed");
        }

        return DepositResponse.builder()
                .depositId(saved.getId())
                .userId(saved.getUserId())
                .status(saved.getStatus().name())
                .amount(saved.getAmount())
                .build();
    }

    @Override
    public DepositResultEvent handleDepositResult(String message) {

        log.info("Processing deposit result event");

        try {
            KafkaEvent event = objectMapper.readValue(message, KafkaEvent.class);

            if (event instanceof DepositSucceededEvent successEvent) {
                return handleDepositSuccess(successEvent);
            }

            if (event instanceof DepositFailedEvent failedEvent) {
                return handleDepositFailure(failedEvent);
            }

            throw new DepositProcessingException("Unsupported deposit event");

        } catch (Exception ex) {
            log.error("Deposit result processing failed", ex);
            throw new DepositProcessingException(ex.getMessage());
        }
    }

    private DepositSucceededEvent handleDepositSuccess(DepositSucceededEvent event) {

        Deposit deposit = depositRepository.findById(event.getDepositId())
                .orElseThrow(() -> new RuntimeException("Deposit not found"));

        deposit.setStatus(DepositStatus.SUCCESS);
        depositRepository.save(deposit);

        log.info("Deposit SUCCESS for id={}", deposit.getId());

        return event;
    }

    private DepositFailedEvent handleDepositFailure(DepositFailedEvent event) {

        Deposit deposit = depositRepository.findById(event.getDepositId())
                .orElseThrow(() -> new RuntimeException("Deposit not found"));

        deposit.setStatus(DepositStatus.FAILED);
        depositRepository.save(deposit);

        log.warn("Deposit FAILED for id={} reason={}", deposit.getId(), event.getReason());

        return event;
    }

    @Override
    public List<TransferResponse> getTransfersBySenderId(Long senderId) {

        log.info("Fetching transfers for sender: {}", senderId);

        return transferRepository.findBySenderId(senderId)
                .stream()
                .map(transferMapper::toResponse)
                .toList();
    }

    @Override
    public TransferResponse getTransferByIdAndSenderId(Long transferId, Long senderId) {

        log.info("Fetching transferId={} for senderId={}", transferId, senderId);

        Transfer transfer = transferRepository.findByIdAndSenderId(transferId, senderId)
                .orElseThrow(() -> {
                    log.warn("Transfer not found or access denied. transferId={}, senderId={}",
                            transferId, senderId);
                    return new TransferNotFoundException("Transfer not found");
                });

        log.info("Transfer found for transferId={} and senderId={}", transferId, senderId);

        return transferMapper.toResponse(transfer);
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

        Transfer savedTransfer = transferRepository.save(transfer);

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
            transferRepository.save(savedTransfer);

            throw new RuntimeException("Transfer event publishing failed");
        }

        return transferMapper.toResponse(savedTransfer);
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

        Transfer transfer = transferRepository.findById(event.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));

        transfer.setStatus(TransferStatus.SUCCEEDED);
        transferRepository.save(transfer);

        log.info("Transfer marked SUCCESS for id={}", transfer.getId());

        return event;
    }

    private TransferFailedEvent handleTransferFailedResult(TransferFailedEvent event) {

        log.info("Handling FAILED event for transferId={}", event.getTransferId());

        Transfer transfer = transferRepository.findById(event.getTransferId())
                .orElseThrow(() -> new TransferNotFoundException("Transfer not found"));

        transfer.setStatus(TransferStatus.FAILED);
        transferRepository.save(transfer);

        log.warn("Transfer marked FAILED for id={} reason={}",
                transfer.getId(), event.getReason());

        return event;
    }
}
