package com.paypal.wallet_service.service;

import com.paypal.wallet_service.kafka.events.DepositInitiatedEvent;
import com.paypal.wallet_service.kafka.events.DepositSucceededEvent;
import com.paypal.wallet_service.kafka.events.TransferInitiatedEvent;
import com.paypal.wallet_service.kafka.events.TransferSucceededEvent;
import com.paypal.wallet_service.model.dto.UserBalanceResponse;

public interface WalletService {

    void createWallet(Long userId);

    UserBalanceResponse getBalance(Long userId);

    DepositSucceededEvent deposit(DepositInitiatedEvent event);

    TransferSucceededEvent transfer(TransferInitiatedEvent event);
}
