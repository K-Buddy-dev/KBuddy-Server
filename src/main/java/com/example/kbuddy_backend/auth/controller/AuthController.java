package com.example.kbuddy_backend.auth.controller;

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
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@GetMapping("/accessToken")
	public ResponseEntity<AccessTokenResponse> getAccessToken(HttpServletRequest request) {
		return ResponseEntity.ok().body(authService.getAccessToken(request));
	}
}
