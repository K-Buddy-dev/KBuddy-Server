package com.example.kbuddy_backend.notification.service;

import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.notification.repository.FCMTokenRepository;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FCMTokenService {
    private final FCMTokenRepository fcmTokenRepository;

    @Transactional
    public FCMToken registerToken(User user, String token, String deviceInfo) {
        Optional<FCMToken> existing = fcmTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            FCMToken fcmToken = existing.get();
            fcmToken.setIsActive(true);
            fcmToken.setUser(user);
            fcmToken.setDeviceInfo(deviceInfo);
            return fcmTokenRepository.save(fcmToken);
        }
        FCMToken newToken = new FCMToken();
        newToken.setUser(user);
        newToken.setToken(token);
        newToken.setDeviceInfo(deviceInfo);
        newToken.setIsActive(true);
        return fcmTokenRepository.save(newToken);
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