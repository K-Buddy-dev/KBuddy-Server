package com.example.kbuddy_backend.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import com.example.kbuddy_backend.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/kbuddy/v1/auth")
@Tag(name = "Auth API", description = "인증 API 목록")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@GetMapping("/accessToken")
	@Operation(summary = "Access Token 발급", description = "쿠키에 포함된 Refresh Token을 통해 Access Token을 발급합니다.")
	public ResponseEntity<AccessTokenResponse> getAccessToken(HttpServletRequest request) {
		return ResponseEntity.ok().body(authService.getAccessToken(request));
	}
}
