package com.example.kbuddy_backend.admin.controller;

import com.example.kbuddy_backend.admin.dto.request.AdminLoginRequest;
import com.example.kbuddy_backend.admin.service.AdminAuthService;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
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
        jakarta.servlet.http.Cookie refreshTokenCookie = new jakarta.servlet.http.Cookie("refreshToken", token.refreshToken());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setMaxAge(refreshTokenExpiration);
        response.addCookie(refreshTokenCookie);
    }
}
