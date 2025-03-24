package com.example.kbuddy_backend.auth.dto.response;

public record AccessTokenResponse(String accessToken, long accessTokenExpireTime) {
	public static AccessTokenResponse of(String accessToken, long accessTokenExpireTime) {
		return new AccessTokenResponse(accessToken, accessTokenExpireTime);
	}
}
