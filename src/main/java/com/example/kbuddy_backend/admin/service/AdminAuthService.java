package com.example.kbuddy_backend.admin.service;

import com.example.kbuddy_backend.admin.config.AdminCredentialsProperties;
import com.example.kbuddy_backend.admin.dto.request.AdminLoginRequest;
import com.example.kbuddy_backend.auth.dto.response.AccessTokenAndRefreshTokenResponse;
import com.example.kbuddy_backend.auth.service.AuthService;
import com.example.kbuddy_backend.common.exception.UnauthorizedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_PRINCIPAL = "ADMIN";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final AdminCredentialsProperties credentialsProperties;
    private final AuthService authService;

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
}
