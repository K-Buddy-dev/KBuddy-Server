package com.example.kbuddy_backend.auth.token;

import com.example.kbuddy_backend.auth.config.CustomUserDetails;
import com.example.kbuddy_backend.auth.dto.response.TokenResponse;
import com.example.kbuddy_backend.auth.exception.TokenExpirationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long accessTokenValidityInSeconds;
    private final long refreshTokenValidityInSeconds;

    public JwtTokenProvider(@Value("${spring.security.jwt.secret-key}") final String secretKey,
                            @Value("${spring.security.jwt.access-token-expiration}") final long accessTokenValidityInSeconds,
                            @Value("${spring.security.jwt.refresh-token-expiration}") final long refreshTokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInSeconds = accessTokenValidityInSeconds;
        this.refreshTokenValidityInSeconds = refreshTokenValidityInSeconds;
    }

    @Override
    public TokenResponse createToken(Authentication authentication, final long tokenValidityInSeconds) {

        final ZonedDateTime now = ZonedDateTime.now();
        final ZonedDateTime tokenValidity = now.plusSeconds(tokenValidityInSeconds);

        String authorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));


        //상담원 사용자인지 일반 사용자인지 구분하기 위한 role 추가

        String token = Jwts.builder()
                .setSubject(authentication.getName())
                .claim("role",authorities)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return TokenResponse.from(token);
    }

    public TokenResponse createAccessToken(Authentication authentication) {
        return createToken(authentication, accessTokenValidityInSeconds);
    }

    public TokenResponse createRefreshToken(Authentication authentication) {
        return createToken(authentication, refreshTokenValidityInSeconds);
    }

    @Override
    public Authentication getAuthentication(final String token) {

        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("role").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        List<String> roles = new ArrayList<>(Arrays.asList(claims.get("role").toString().split(",")));
        CustomUserDetails principal = new CustomUserDetails(claims.getSubject(), roles);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    @Override
    public String getPayload(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public boolean validateToken(final String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            Date tokenExpirationDate = claims.getBody().getExpiration();
            validateTokenExpiration(tokenExpirationDate);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private void validateTokenExpiration(Date tokenExpirationDate) {
        if (tokenExpirationDate.before(new Date(System.currentTimeMillis()))) {
            throw new TokenExpirationException();
        }
    }

    @Override
    public long getAccessTokenExpiryDuration() {
        return accessTokenValidityInSeconds;
    }

    @Override
    public long getRefreshTokenExpiryDuration() {
        return refreshTokenValidityInSeconds;
    }

    //사용자 role 반환
    public String getUserRole(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

}
