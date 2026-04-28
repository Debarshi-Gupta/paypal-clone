package com.paypal.wallet_service.service;

import com.paypal.wallet_service.exception.InsufficientBalanceException;
import com.paypal.wallet_service.exception.WalletAlreadyExistsException;
import com.paypal.wallet_service.exception.WalletNotFoundException;
import com.paypal.wallet_service.kafka.events.*;
import com.paypal.wallet_service.model.dto.DepositResponse;
import com.paypal.wallet_service.model.entity.Wallet;
import com.paypal.wallet_service.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {

    private final WalletRepository repository;

    public void createWallet(Long userId) {

        log.info("Creating wallet for user: {}", userId);

        repository.findByUserId(userId).ifPresent(wallet -> {
            log.warn("Wallet already exists for user: {}", userId);
            throw new WalletAlreadyExistsException("Wallet already exists");
        });

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .build();

        repository.save(wallet);

        log.info("Wallet created successfully for user: {}", userId);
    }

    public BigDecimal getBalance(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"))
                .getBalance();
    }

    @Transactional
    @Override
    public DepositSucceededEvent deposit(DepositInitiatedEvent event) {

        log.info("Depositing {} to wallet of user {}", event.getAmount(), event.getUserId());

        if (event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Wallet userWallet = repository.findByUserId(event.getUserId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        userWallet.setBalance(userWallet.getBalance().add(event.getAmount()));

        log.info("Deposit successful. New balance for user {} is {}", event.getUserId(), userWallet.getBalance());

        return DepositSucceededEvent.builder()
                .depositId(event.getDepositId())
                .userId(event.getUserId())
                .amount(event.getAmount())
                .userBalance(userWallet.getBalance())
                .build();
    }

    @Transactional
    @Override
    public TransferSucceededEvent transfer(TransferInitiatedEvent event) {

        log.info("Initiating transfer from {} to {} amount {}", event.getSenderId(), event.getReceiverId(), event.getAmount());


        if (event.getSenderId().equals(event.getReceiverId())) {
            throw new IllegalArgumentException("Sender and receiver cannot be same");
        }

        Wallet senderWallet = repository.findByUserId(event.getSenderId())
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        Wallet receiverWallet = repository.findByUserId(event.getReceiverId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        if (senderWallet.getBalance().compareTo(event.getAmount()) < 0) {
            log.warn("Insufficient balance for user {}", event.getSenderId());
            throw new InsufficientBalanceException("Insufficient balance");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(event.getAmount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(event.getAmount()));

        log.info("Transfer successful: {} -> {} amount {}", event.getSenderId(), event.getReceiverId(), event.getAmount());

        return TransferSucceededEvent.builder()
                .transferId(event.getTransferId())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .amount(event.getAmount())
                .senderBalance(senderWallet.getBalance())
                .receiverBalance(receiverWallet.getBalance())
                .build();
    }
}
