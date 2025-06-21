package com.example.kbuddy_backend.notification.repository;

import com.example.kbuddy_backend.notification.entity.Notification;
import com.example.kbuddy_backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 특정 사용자의 알림 목록을 페이징하여 조회
     */
    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);
    
    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    long countByReceiverAndIsReadFalse(User receiver);
    
    /**
     * 특정 사용자의 모든 알림을 읽음 상태로 변경
     */
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver = :receiver AND n.isRead = false")
    void markAllAsRead(@Param("receiver") User receiver);
    
    /**
     * 특정 사용자의 읽지 않은 알림 목록 조회
     */
    List<Notification> findByReceiverAndIsReadFalseOrderByCreatedAtDesc(User receiver);
} 