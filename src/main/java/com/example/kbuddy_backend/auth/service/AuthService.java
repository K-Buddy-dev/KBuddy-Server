package com.example.kbuddy_backend.auth.service;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.user.constant.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public AccessTokenAndRefreshTokenResponse createToken(Authentication authentication) {

        final TokenResponse accessToken = jwtTokenProvider.createAccessToken(authentication);
        final TokenResponse refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        return AccessTokenAndRefreshTokenResponse.of(accessToken.token(), refreshToken.token(),
                jwtTokenProvider.getAccessTokenExpiryDuration(), jwtTokenProvider.getRefreshTokenExpiryDuration());
    }

}
