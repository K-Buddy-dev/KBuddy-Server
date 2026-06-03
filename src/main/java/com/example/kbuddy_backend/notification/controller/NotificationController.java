package com.example.kbuddy_backend.notification.controller;

import com.example.kbuddy_backend.common.config.CurrentUser;
import com.example.kbuddy_backend.notification.dto.NotificationResponse;
import com.example.kbuddy_backend.notification.service.NotificationService;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @CurrentUser User user,
            Pageable pageable) {
        return ResponseEntity.ok(notificationService.getUserNotifications(user, pageable)
                .map(NotificationResponse::from));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(@CurrentUser User user) {
        return ResponseEntity.ok(notificationService.getUnreadNotificationCount(user));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@CurrentUser User user) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user).stream()
                .map(NotificationResponse::from)
                .toList());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId, @CurrentUser User user) {
        notificationService.markAsRead(notificationId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@CurrentUser User user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.noContent().build();
    }
} 