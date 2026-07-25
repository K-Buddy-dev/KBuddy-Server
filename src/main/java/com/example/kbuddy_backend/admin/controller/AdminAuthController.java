package com.example.kbuddy_backend.admin.controller;

import com.example.kbuddy_backend.admin.dto.request.AdminLoginRequest;
import com.example.kbuddy_backend.admin.service.AdminAuthService;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.kbuddy_backend.admin.config.AdminAuthCookie.*;

@RestController
@RequestMapping("/kbuddy/v1/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    @Value("${spring.security.jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(
            @Valid @RequestBody AdminLoginRequest request,
            HttpServletResponse response
    ) {
        AccessTokenAndRefreshTokenResponse token = adminAuthService.login(request);
        setRefreshTokenInCookie(response, token);
        return ResponseEntity.ok(token);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.refreshAccessToken(request));
    }

    private void setRefreshTokenInCookie(HttpServletResponse response, AccessTokenAndRefreshTokenResponse token) {
        /*
         * 일반 사용자 refresh token과 관리자 refresh token이
         * 서로 덮어쓰거나 잘못 사용되는 것을 방지합니다.
         */
        Cookie cookie = new Cookie(ADMIN_TOKEN_NAME, token.refreshToken());
        // 관리자 API 요청에만 이 쿠키가 전송됩니다.
        cookie.setPath(ADMIN_TOKEN_PATH);
        // HTTPS 연결에서만 쿠키를 전송합니다.
        cookie.setSecure(true);
        /*
         * JavaScript에서 document.cookie로 읽을 수 없도록 합니다.
         * 토큰 탈취 위험을 줄이기 위한 설정입니다.
         */
        cookie.setHttpOnly(true);
        // refresh token 만료 시간과 쿠키 만료 시간을 맞춥니다.
        cookie.setMaxAge(refreshTokenExpiration);
        response.addCookie(cookie);
    }
}
