package com.paypal.wallet_service.service;

import com.paypal.wallet_service.model.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {

    void createWallet(Long userId);

    BigDecimal getBalance(Long userId);

    void deposit(Long userId, BigDecimal amount);

    void transfer(Long senderId, Long receiverId, BigDecimal amount);
}
