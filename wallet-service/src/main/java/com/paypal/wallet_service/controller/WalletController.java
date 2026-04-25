package com.paypal.wallet_service.controller;

import com.paypal.wallet_service.model.dto.DepositRequest;
import com.paypal.wallet_service.model.dto.DepositResponse;
import com.paypal.wallet_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService service;

    @PostMapping("/create")
    public ResponseEntity<String> createWallet(@RequestParam Long userId) {

        log.info("API call: Create wallet for user {}", userId);

        service.createWallet(userId);

        return ResponseEntity.ok("Wallet created successfully");
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@RequestParam Long userId) {

        log.info("API call: get balance for user {}", userId);

        return ResponseEntity.ok(service.getBalance(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @RequestParam Long userId,
            @Valid @RequestBody DepositRequest request) {

        log.info("API call: deposit for user {}", userId);

        return ResponseEntity.ok(service.deposit(userId, request.getAmount()));
    }
}
