package com.paypal.wallet_service.service;

import com.paypal.wallet_service.exception.InsufficientBalanceException;
import com.paypal.wallet_service.exception.WalletAlreadyExistsException;
import com.paypal.wallet_service.exception.WalletNotFoundException;
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
    public void deposit(Long userId, BigDecimal amount) {

        log.info("Depositing {} to wallet of user {}", amount, userId);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        Wallet wallet = repository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(amount));

        log.info("Deposit successful. New balance for user {} is {}", userId, wallet.getBalance());
    }

    @Transactional
    public void transfer(Long senderId, Long receiverId, BigDecimal amount) {

        log.info("Initiating transfer from {} to {} amount {}", senderId, receiverId, amount);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver cannot be same");
        }

        Wallet sender = repository.findByUserId(senderId)
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        Wallet receiver = repository.findByUserId(receiverId)
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        if (sender.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for user {}", senderId);
            throw new InsufficientBalanceException("Insufficient balance");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        log.info("Transfer successful: {} -> {} amount {}", senderId, receiverId, amount);
    }
}
