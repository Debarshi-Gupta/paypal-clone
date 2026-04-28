package com.paypal.notification_service.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications",
        indexes = {
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_read", columnList = "is_read")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "notification_type")
    private NotificationType notificationType;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}