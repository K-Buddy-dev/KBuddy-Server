package com.example.kbuddy_backend.admin.controller;

import com.example.kbuddy_backend.admin.dto.request.AdminLoginRequest;
import com.example.kbuddy_backend.admin.service.AdminAuthService;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/login")
    public ResponseEntity<AccessTokenAndRefreshTokenResponse> login(
            @Valid @RequestBody AdminLoginRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @GetMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refreshAccessToken(
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.refreshAccessToken(request));
    }
}
