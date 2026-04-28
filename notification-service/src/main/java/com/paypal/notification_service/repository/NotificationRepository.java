package com.paypal.notification_service.repository;

import com.paypal.notification_service.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
