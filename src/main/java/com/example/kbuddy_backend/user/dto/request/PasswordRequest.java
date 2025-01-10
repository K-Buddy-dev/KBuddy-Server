package com.example.kbuddy_backend.user.dto.request;

public record PasswordRequest(String password) {
	public static PasswordRequest of(String password) {
		return new PasswordRequest(password);
	}
}
