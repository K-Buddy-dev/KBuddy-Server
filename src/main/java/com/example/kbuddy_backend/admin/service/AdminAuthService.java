package com.example.kbuddy_backend.admin.service;

import com.example.kbuddy_backend.admin.config.AdminCredentialsProperties;
import com.example.kbuddy_backend.admin.dto.request.AdminLoginRequest;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenResponse;
import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.auth.exception.TokenNotFoundException;
import com.example.kbuddy_backend.auth.service.AuthService;
import com.example.kbuddy_backend.auth.token.JwtTokenProvider;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import static com.example.kbuddy_backend.admin.config.AdminAuthCookie.ADMIN_TOKEN_NAME;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_PRINCIPAL = "ADMIN";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final AdminCredentialsProperties credentialsProperties;
    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AccessTokenAndRefreshTokenResponse login(AdminLoginRequest request) {
        if (!credentialsProperties.matches(request.id(), request.password())) {
            throw new UnauthorizedException("관리자 계정 정보가 올바르지 않습니다.");
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                ADMIN_PRINCIPAL,
                null,
                List.of(new SimpleGrantedAuthority(ADMIN_ROLE))
        );
        return authService.createToken(authentication);
    }

    public AccessTokenResponse refreshAccessToken(HttpServletRequest request) {
        String refreshToken = extractRefreshToken(request);
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }
        String role = jwtTokenProvider.getUserRole(refreshToken);
        if (role == null || !role.contains(ADMIN_ROLE)) {
            throw new UnauthorizedException("관리자 권한이 없습니다.");
        }
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        TokenResponse accessToken = jwtTokenProvider.createAccessToken(authentication);
        return AccessTokenResponse.of(accessToken.token(), jwtTokenProvider.getAccessTokenExpiryDuration());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new TokenNotFoundException();
        }
        /*
         * 기존에는 "refreshToken"을 찾았기 때문에
         * 일반 사용자 토큰도 선택될 수 있었습니다.
         *
         * 변경 후에는 "adminRefreshToken"만 찾습니다.
         */
        return Arrays.stream(request.getCookies())
                .filter(cookie -> ADMIN_TOKEN_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(TokenNotFoundException::new);
    }
}
