package com.paypal.transaction_service.controller;

import com.paypal.transaction_service.model.dto.CreateTransferRequest;
import com.paypal.transaction_service.model.dto.DepositRequest;
import com.paypal.transaction_service.model.dto.DepositResponse;
import com.paypal.transaction_service.model.dto.TransferResponse;
import com.paypal.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API call: get balance for user {}", userId);

        return ResponseEntity.ok(service.getBalance(userId));
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API call: deposit for user {}", userId);

        return ResponseEntity.ok(service.deposit(userId, request));
    }

    @PostMapping("/transfers")
    public TransferResponse initiateTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        Long senderId = extractUserId(authentication);

        log.info("API initiateTransfer called by {}", email);

        return service.initiateTransfer(senderId, request);
    }

    @GetMapping("/transfers")
    public List<TransferResponse> getAllTransfers(Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("Fetching transfers for logged-in user");

        return service.getTransfersBySenderId(senderId);
    }

    @GetMapping("/transfers/{id}")
    public TransferResponse getTransferById(
            @PathVariable("id") Long transferId,
            Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("API getTransferById called for id={} by user={}", transferId, senderId);

        return service.getTransferById(transferId, senderId);
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }
}
