package com.example.kbuddy_backend.auth.service;

import java.util.Arrays;
import java.util.Optional;

import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.user.constant.UserRole;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

    //todo: refresh token을 redis에 저장된 값과 비교해야 함.
    public AccessTokenResponse getAccessToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new IllegalArgumentException("refresh token이 없습니다.");
        }
        Optional<String> refreshToken = Arrays.stream(request.getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName())) // refreshToken 쿠키 찾기
            .map(Cookie::getValue)
            .findFirst();

        if(!jwtTokenProvider.validateToken(refreshToken.get())) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        }
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken.get());
        TokenResponse accessToken = jwtTokenProvider.createAccessToken(authentication);
        return AccessTokenResponse.of(accessToken.token(), jwtTokenProvider.getAccessTokenExpiryDuration());
    }
}
