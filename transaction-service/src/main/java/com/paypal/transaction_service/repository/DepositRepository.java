package com.paypal.transaction_service.repository;

import com.paypal.transaction_service.model.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    Optional<Deposit> findByIdAndUserId(Long id, Long userId);

    List<Deposit> findByUserId(Long userId);
}
