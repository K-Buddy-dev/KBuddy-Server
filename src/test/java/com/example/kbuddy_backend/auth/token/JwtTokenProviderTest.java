package com.example.kbuddy_backend.auth.token;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.common.WebMVCTest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

class JwtTokenProviderTest extends WebMVCTest {

    private static final String TEST_SECRET_KEY = "9d0bd354d2a68141d2ced83c26fe3fb72046783c19e7b727a45804d7d80c96a1541f9decbc3833519bd168ff7735d15a0e0737f40b20977bece9d8c0220425a1";

    @DisplayName("만료된 토큰인지 확인한다.")
    @Test
    void isExpiredToken() {

        //given
        final Date now = new Date();
        final Date validity = new Date(now.getTime() + 3600000);
        final SecretKey keys = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        //when
        final String token = Jwts.builder()
                .setSubject("test")
                .setIssuedAt(new Date(now.getTime() - 100))
                .setExpiration(validity)
                .signWith(keys, SignatureAlgorithm.HS256)
                .compact();

        //then
        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @DisplayName("유효한 토큰을 검증한다.")
    @Test
    void isValidatedToken() {
        //given

        //when
        TokenResponse token = createTokens();

        //then
        assertThat(tokenProvider.validateToken(token.token())).isTrue();
    }


    @DisplayName("유효하지 않은 토큰을 검증한다.")
    @Test
    void isUnValidatedToken() {
        //given
        TokenResponse token = createTokens();
        String unValidatedToken = token.token() + "invalid";

        //then
        assertThat(tokenProvider.validateToken(unValidatedToken)).isFalse();
    }

    @DisplayName("토큰에 포함된 인증 정보를 확인한다.")
    @Test
    void checkUserAuthenticationInToken() {
        //given
        TokenResponse tokens = createTokens();

        //when
        Authentication authentication = tokenProvider.getAuthentication(tokens.token());
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        //then
        assertThat(authentication.getName()).isEqualTo("email");
        assertThat(authorities).hasSize(1).extracting(GrantedAuthority::getAuthority).contains("ROLE_USER");
    }

    private TokenResponse createTokens() {

        final List<GrantedAuthority> grantedAuthorities = List.of(() -> "ROLE_USER");
        final UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken("email", "password", grantedAuthorities);
        return tokenProvider.createToken(authenticationToken, 3600);
    }
}
