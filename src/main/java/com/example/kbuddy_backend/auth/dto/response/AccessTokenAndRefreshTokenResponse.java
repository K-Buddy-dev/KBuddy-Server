package com.example.kbuddy_backend.auth.dto.response;

public record AccessTokenAndRefreshTokenResponse(String accessToken, String refreshToken, long accessTokenExpireTime,
                                                 long refreshTokenExpireTime) {
    public static AccessTokenAndRefreshTokenResponse of(String accessToken, String refreshToken,
                                                        long accessTokenExpireTime, long refreshTokenExpireTime) {
        return new AccessTokenAndRefreshTokenResponse(accessToken, refreshToken, accessTokenExpireTime,
                refreshTokenExpireTime);
    }
}
