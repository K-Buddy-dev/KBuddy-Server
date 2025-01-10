package com.example.kbuddy_backend.auth.token;

import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.user.constant.UserRole;
import org.springframework.security.core.Authentication;

public interface TokenProvider {

    TokenResponse createToken(final Authentication authentication, final long tokenValidityInMilliseconds);

    String getPayload(final String token);

    Authentication getAuthentication(final String token);

    boolean validateToken(final String token);

    long getAccessTokenExpiryDuration();
    long getRefreshTokenExpiryDuration();
}
