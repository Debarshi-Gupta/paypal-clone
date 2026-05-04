package com.paypal.reward_service.repository;

import com.paypal.reward_service.model.entity.TransferReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRewardRepository extends JpaRepository<TransferReward, Long> {

    Optional<TransferReward> findByTransferId(Long transferId);

    List<TransferReward> findByUserId(Long userId);

    Optional<TransferReward> findByIdAndUserId(Long id, Long userId);
}
