package com.example.kbuddy_backend.notification.repository;

import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    Optional<FCMToken> findByToken(String token);
    List<FCMToken> findByUserAndIsActiveTrue(User user);
    List<FCMToken> findByUser(User user);
} 