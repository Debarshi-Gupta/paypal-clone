package com.paypal.reward_service.controller;

import com.paypal.reward_service.model.dto.TransferRewardResponse;
import com.paypal.reward_service.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
public class RewardController {

    private final RewardService rewardService;

    @GetMapping("/points")
    public ResponseEntity<Integer> getUserRewardPoints(Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getUserRewardPoints called for userId={}", userId);

        return ResponseEntity.ok(rewardService.getUserRewardPoints(userId));
    }

    @GetMapping
    public ResponseEntity<List<TransferRewardResponse>> getTransferRewardsByUserId(Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getTransferRewardsByUserId called for userId={}", userId);

        return ResponseEntity.ok(rewardService.getTransferRewardsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferRewardResponse> getTransferRewardByIdAndUserId(
            @PathVariable("id") Long rewardId,
            Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getTransferRewardByIdAndUserId called for rewardId={} by userId={}",
                rewardId, userId);

        return ResponseEntity.ok(rewardService.getTransferRewardByIdAndUserId(rewardId, userId));
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }
}
