package com.paypal.transaction_service.repository;

import com.paypal.transaction_service.model.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySenderId(Long senderId);

    List<Transfer> findByReceiverId(Long receiverId);

    Optional<Transfer> findByIdAndSenderId(Long id, Long senderId);
}
