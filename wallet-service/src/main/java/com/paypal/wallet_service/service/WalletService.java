package com.paypal.wallet_service.service;

import com.paypal.wallet_service.kafka.events.DepositInitiatedEvent;
import com.paypal.wallet_service.kafka.events.DepositSucceededEvent;
import com.paypal.wallet_service.kafka.events.TransferInitiatedEvent;
import com.paypal.wallet_service.kafka.events.TransferSucceededEvent;
import com.paypal.wallet_service.model.dto.DepositResponse;
import com.paypal.wallet_service.model.entity.Wallet;

import java.math.BigDecimal;

public interface WalletService {

    void createWallet(Long userId);

    BigDecimal getBalance(Long userId);

    DepositSucceededEvent deposit(DepositInitiatedEvent event);

    TransferSucceededEvent transfer(TransferInitiatedEvent event);
}
