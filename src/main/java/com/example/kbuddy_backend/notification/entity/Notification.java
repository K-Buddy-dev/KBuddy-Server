package com.example.kbuddy_backend.notification.entity;

import com.example.kbuddy_backend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 알림을 나타내는 엔티티
 * 사용자에게 전달되는 다양한 유형의 알림을 저장
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_notification_receiver", columnList = "receiver_id"),
           @Index(name = "idx_notification_created_at", columnList = "created_at"),
           @Index(name = "idx_notification_read_status", columnList = "is_read")
       })
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    @NotNull
    private User receiver;

    @Column(name = "is_read", nullable = false)
    @NotNull
    private Boolean isRead = false;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotNull
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private NotificationType type;

    /**
     * 알림 생성 시 사용할 정적 팩토리 메서드
     */
    public static Notification createNotification(User receiver, String message, NotificationType type) {
        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        return notification;
    }
} 