package com.example.kbuddy_backend.notification.entity;

/**
 * 알림의 종류를 정의하는 enum
 * 각 알림 타입은 특정 이벤트에 대한 알림을 나타냄
 */
public enum NotificationType {
    FREE_NOTIFICATION,           // 일반 알림
    QNA_LIKE_NOTIFICATION,      // QnA 좋아요 알림
    QNA_COMMENT_NOTIFICATION,   // QnA 댓글 알림
    BLOG_LIKE_NOTIFICATION,     // 블로그 좋아요 알림
    BLOG_COMMENT_NOTIFICATION   // 블로그 댓글 알림
} 