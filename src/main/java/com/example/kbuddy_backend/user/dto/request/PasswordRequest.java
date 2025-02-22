package com.example.kbuddy_backend.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record PasswordRequest(@NotNull String password) {
	public static PasswordRequest of(String password) {
		return new PasswordRequest(password);
	}
}
