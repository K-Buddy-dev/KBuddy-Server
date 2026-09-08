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

    @Column(nullable = false)
    @NotNull
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotNull
    private String message;

    @Column
    private String targetId;

    // 같은 업무 이벤트의 알림을 한 건으로 제한한다. 일반 알림은 null을 사용해 기존 동작을 유지한다.
    @Column(name = "event_key", unique = true, length = 100)
    private String eventKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private NotificationType type;

    public static Notification createNotification(User receiver, String title, String message, NotificationType type, String targetId) {
        return createNotification(null, receiver, title, message, type, targetId);
    }

    public static Notification createNotification(
            String eventKey,
            User receiver,
            String title,
            String message,
            NotificationType type,
            String targetId) {
        Notification notification = new Notification();
        notification.setEventKey(eventKey);
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetId(targetId);
        notification.setIsRead(false);
        return notification;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
