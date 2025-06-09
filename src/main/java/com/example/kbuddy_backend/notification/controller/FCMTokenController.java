package com.example.kbuddy_backend.notification.controller;

import com.example.kbuddy_backend.notification.entity.FCMToken;
import com.example.kbuddy_backend.notification.service.FCMTokenService;
import com.example.kbuddy_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/fcm-tokens")
@RequiredArgsConstructor
public class FCMTokenController {
    private final FCMTokenService fcmTokenService;

    /**
     * 유저의 새로운 토큰 등록
     */
    @PostMapping
    public ResponseEntity<FCMToken> registerToken(
            @AuthenticationPrincipal User user,
            @RequestParam String token,
            @RequestParam(required = false) String deviceInfo) {
        FCMToken saved = fcmTokenService.registerToken(user, token, deviceInfo);
        return ResponseEntity.ok(saved);
    }

    /**
     * 토큰 비활성화
     */
    @DeleteMapping
    public ResponseEntity<Void> deactivateToken(@RequestParam String token) {
        fcmTokenService.deactivateToken(token);
        return ResponseEntity.ok().build();
    }

    /**
     * 유저의 모든 활성 토큰 조회
     */
    @GetMapping
    public ResponseEntity<List<FCMToken>> getActiveTokens(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(fcmTokenService.getActiveTokens(user));
    }
} 