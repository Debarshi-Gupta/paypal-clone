package com.paypal.reward_service.service;

import com.paypal.reward_service.exception.TransferRewardNotFoundException;
import com.paypal.reward_service.exception.TransferRewardProcessingException;
import com.paypal.reward_service.exception.UserNotFoundException;
import com.paypal.reward_service.kafka.events.TransferSucceededEvent;
import com.paypal.reward_service.model.dto.TransferRewardResponse;
import com.paypal.reward_service.model.dto.UserResponse;
import com.paypal.reward_service.model.entity.TransferReward;
import com.paypal.reward_service.model.entity.UserRewardBalance;
import com.paypal.reward_service.repository.TransferRewardRepository;
import com.paypal.reward_service.repository.UserRewardBalanceRepository;
import com.paypal.reward_service.service.feign.UserClient;
import com.paypal.reward_service.service.mapper.TransferRewardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardServiceImpl implements RewardService {

    private final TransferRewardRepository transferRewardRepository;
    private final UserRewardBalanceRepository userRewardBalanceRepository;
    private final TransferRewardMapper transferRewardMapper;
    private final UserClient userClient;

    @Value("${reward.points.per.unit}")
    private Integer pointsPerUnit;

    @Override
    public List<TransferRewardResponse> getTransferRewardsByUserId(Long userId) {

        log.info("Fetching all transfer rewards for userId={}", userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        List<TransferReward> transferRewards = transferRewardRepository.findByUserId(user.getId());

        return transferRewards.stream().map(transferRewardMapper::toResponse).toList();
    }

    @Override
    public TransferRewardResponse getTransferRewardByIdAndUserId(Long transferRewardId, Long userId) {

        log.info("Fetching transfer reward id {} for userId={}", transferRewardId, userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        TransferReward transferReward = transferRewardRepository.findByIdAndUserId(transferRewardId, user.getId())
                .orElseThrow(() -> {
                    log.warn("Transfer Reward not found or access denied. transferRewardId={}, userId={}",
                            transferRewardId, user.getId());
                    return new TransferRewardNotFoundException("Transfer reward not found with transferRewardId " + transferRewardId + " for userId=" + user.getId());
                });

        log.info("Transfer reward found with transferRewardId " + transferRewardId + " for userId=" + user.getId());

        return transferRewardMapper.toResponse(transferReward);
    }

    @Override
    public Integer getUserRewardPoints(Long userId) {

        log.info("Fetching reward points for userId={}", userId);

        UserResponse user;

        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error("User Id {} doesn't exist", userId);
            throw new UserNotFoundException("User Id not found");
        }

        Integer userRewardPoints =  userRewardBalanceRepository.findById(user.getId())
                .map(UserRewardBalance::getTotalRewardPoints)
                .orElse(0);

        log.info("Fetched reward points {} for userId={}", userRewardPoints, user.getId());

        return userRewardPoints;
    }

    @Override
    @Transactional
    public void processReward(TransferSucceededEvent event) {

        Long transferId = event.getTransferId();
        Long userId = event.getSenderId();

        log.info("Processing reward for transferId={}", transferId);

        if (transferRewardRepository.findByTransferId(transferId).isPresent()) {
            log.warn("Reward already processed for transferId={}", transferId);
            return;
        }

        int points = calculatePoints(event.getAmount());

        try {

            TransferReward rewardTxn = TransferReward.builder()
                    .transferId(transferId)
                    .userId(userId)
                    .transferAmount(event.getAmount())
                    .rewardPoints(points)
                    .build();

            transferRewardRepository.save(rewardTxn);

            UserRewardBalance balance = userRewardBalanceRepository
                    .findById(userId)
                    .orElse(UserRewardBalance.builder()
                            .userId(userId)
                            .totalRewardPoints(0)
                            .build());

            balance.setTotalRewardPoints(balance.getTotalRewardPoints() + points);

            userRewardBalanceRepository.save(balance);

            log.info("Reward updated: userId={}, pointsAdded={}, totalPoints={}",
                    userId, points, balance.getTotalRewardPoints());

        } catch (Exception ex) {
            log.error("Failed to process reward for transferId={}", transferId, ex);
            throw new TransferRewardProcessingException("Reward processing failed");
        }
    }

    private int calculatePoints(BigDecimal amount) {
        return amount.divide(BigDecimal.valueOf(pointsPerUnit)).intValue();
    }
}
