package com.paypal.notification_service.controller;

import com.paypal.notification_service.model.dto.NotificationResponse;
import com.paypal.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(Authentication authentication) {

        Long userId = extractUserId(authentication);

        log.info("API getNotificationsByUserId called for userId={}", userId);

        return ResponseEntity.ok(notificationService.getNotificationsByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationByIdAndUserId(
            @PathVariable("id") Long notificationId,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        log.info("API getNotificationByIdAndUserId called for notificationId={} and userId={}", notificationId, userId);

        return ResponseEntity.ok(notificationService.getNotificationByIdAndUserId(notificationId, userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(
            @PathVariable("id") Long notificationId,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        log.info("API markNotificationAsRead called for notificationId={} and userId={}", notificationId, userId);

        return ResponseEntity.ok(notificationService.markNotificationAsRead(notificationId, userId));
    }

    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getCredentials();
    }
}
