package com.paypal.transaction_service.controller;

import com.paypal.transaction_service.model.dto.CreateTransactionRequest;
import com.paypal.transaction_service.model.dto.TransactionResponse;
import com.paypal.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService service;

    @PostMapping
    public TransactionResponse createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        Long senderId = extractUserId(authentication);

        log.info("API createTransaction called by {}", email);

        return service.createTransaction(senderId, request);
    }

    @GetMapping("/sent")
    public List<TransactionResponse> getMyTransactions(Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("Fetching transactions for logged-in user");

        return service.getTransactionsBySender(senderId);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransactionById(
            @PathVariable("id") Long transactionId,
            Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("API getTransactionById called for id={} by user={}", transactionId, senderId);

        return service.getTransactionById(transactionId, senderId);
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }
}
