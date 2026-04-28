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

        log.info("API getBalance called for userId={}", userId);

        return ResponseEntity.ok(service.getBalance(userId));
    }

    @GetMapping("/deposits")
    public List<DepositResponse> getDepositsByUserId(Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getDepositsByUserId called for userId={}", userId);

        return service.getDepositsByUserId(userId);
    }

    @GetMapping("/deposits/{id}")
    public DepositResponse getDepositByIdAndUserId(
            @PathVariable("id") Long depositId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getDepositByIdAndUserId called for depositId={} by userId={}", depositId, userId);

        return service.getDepositByIdAndUserId(depositId, userId);
    }

    @PostMapping("/deposits")
    public ResponseEntity<DepositResponse> initiateDeposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API deposit called for userId={}", userId);

        return ResponseEntity.ok(service.initiateDeposit(userId, request));
    }

    @GetMapping("/transfers")
    public List<TransferResponse> getTransfersBySenderId(Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("API getTransfersBySenderId called for userId={}", senderId);

        return service.getTransfersBySenderId(senderId);
    }

    @GetMapping("/transfers/{id}")
    public TransferResponse getTransferByIdAndSenderId(
            @PathVariable("id") Long transferId,
            Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("API getTransferByIdAndSenderId called for transferId={} by userId={}", transferId, senderId);

        return service.getTransferByIdAndSenderId(transferId, senderId);
    }

    @PostMapping("/transfers")
    public TransferResponse initiateTransfer(
            @Valid @RequestBody CreateTransferRequest request,
            Authentication authentication) {

        Long senderId = extractUserId(authentication);

        log.info("API initiateTransfer called for senderId={}", senderId);

        return service.initiateTransfer(senderId, request);
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }
}
