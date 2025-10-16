package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.DevicePlatform;
import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.notification.repository.FCMTokenRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FCMTokenService {
    private final FCMTokenRepository fcmTokenRepository;

    @Transactional
    public void registerToken(User user, String token, String deviceInfo, DevicePlatform platform) {
        Optional<FCMToken> existing = fcmTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            FCMToken fcmToken = existing.get();
            fcmToken.setIsActive(true);
            fcmToken.setUser(user);
            fcmToken.setUpdatedAt(LocalDateTime.now());
            fcmToken.setDeviceInfo(deviceInfo);
            fcmToken.setPlatform(platform);
            return;
        }
        FCMToken newToken = new FCMToken();
        newToken.setUser(user);
        newToken.setToken(token);
        newToken.setDeviceInfo(deviceInfo);
        newToken.setPlatform(platform);
        newToken.setIsActive(true);
        fcmTokenRepository.save(newToken);
    }

    @Transactional(readOnly = true)
    public Optional<FCMToken> findByToken(String token) {
        return fcmTokenRepository.findByToken(token);
    }

    @Transactional
    public void deactivateToken(String token) {
        fcmTokenRepository.findByToken(token).ifPresent(fcmToken -> {
            fcmToken.setIsActive(false);
            fcmTokenRepository.save(fcmToken);
        });
    }

    @Transactional(readOnly = true)
    public List<FCMToken> getActiveTokens(User user) {
        return fcmTokenRepository.findByUserAndIsActiveTrue(user);
    }

    @Transactional
    public void deactivateAllUserTokens(User user) {
        List<FCMToken> userTokens = fcmTokenRepository.findByUser(user);
        for (FCMToken token : userTokens) {
            token.setIsActive(false);
        }
        fcmTokenRepository.saveAll(userTokens);
    }
} 