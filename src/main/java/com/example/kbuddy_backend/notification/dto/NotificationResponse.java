package com.example.kbuddy_backend.notification.dto;

import com.example.kbuddy_backend.notification.entity.Notification;
import com.example.kbuddy_backend.notification.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        NotificationType type,
        boolean isRead,
        String targetId,
        LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getTargetId(),
                notification.getCreatedAt());
    }
}
