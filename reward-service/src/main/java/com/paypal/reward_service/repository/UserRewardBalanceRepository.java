package com.paypal.reward_service.repository;

import com.paypal.reward_service.model.entity.UserRewardBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRewardBalanceRepository extends JpaRepository<UserRewardBalance, Long> {
}
